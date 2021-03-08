WITH all_jobs AS (SELECT job_id, job_name
                  FROM experiments.jobs
                  WHERE jar_id = ?),
     all_tasks AS (
         SELECT max(task_name)     as task_name,
                max(job_name)      as job_name,
                max(task_id)       as task_id,
                max(task_position) as task_position
         FROM experiments.tasks t
                  LEFT JOIN all_jobs j ON t.job_id = j.job_id
         WHERE j.job_id NOTNULL
         GROUP BY task_id),
     add_previous_tasks AS (
         SELECT *,
                LAG(task_id, 1) OVER (
                    ORDER BY task_position
                    ) previous_task_id
         FROM all_tasks),
     metrics AS (
         SELECT t.task_name,
                t.job_name,
                om1.job_id,
                om1.task_parallelism,
                om1.max_records_out,
                om1.max_records_in,
                om1.experiment_id,
                om1.max_backpresure,
                om2.max_backpresure as previous_task_backpressure,
                task_position
         FROM add_previous_tasks t
                  LEFT JOIN experiments.metrics om1
                            ON (t.task_id = om1.task_id)
                  LEFT JOIN experiments.metrics om2
                            ON (t.previous_task_id = om2.task_id AND
                                om1.experiment_id = om2.experiment_id)),
     jobs_to_jar_id AS (
         SELECT *
         FROM metrics m
                  lEFT JOIN all_jobs j ON (m.job_id = j.job_id)
         WHERE j.job_id NOTNULL),
     last_oparator AS (SELECT task_name
                       FROM all_tasks
                       ORDER BY task_position DESC
                       FETCH FIRST 1 ROW ONLY
     ),
     intermediate_tasks as (
         SELECT task_name,
                task_parallelism,
                array_agg(max_records_out ::int) as max_throughput,
                avg(max_records_out) ::int       as avg_max_throughput,
                max(max_records_out) :: int      as highest_max_throughput,
                task_position,
                True                             as backpressure_condition_holds
         FROM jobs_to_jar_id j
         WHERE previous_task_backpressure > 0.5
           AND max_backpresure < 0.5
           AND 0 < task_position
           AND task_name NOT IN (SELECT task_name FROM last_oparator)
         GROUP BY task_name, task_parallelism, task_position
         ORDER BY task_name, task_parallelism),
     source_task as (
         SELECT task_name,
                task_parallelism,
                array_agg(max_records_out ::int) as max_throughput,
                avg(max_records_out) ::int       as avg_max_throughput,
                max(max_records_out) :: int      as highest_max_throughput,
                task_position,
                True                             as backpressure_condition_holds

         FROM jobs_to_jar_id j
         WHERE task_position = 0
         GROUP BY task_name, task_parallelism, task_position
         ORDER BY task_name, task_parallelism),

     sink_task as (
         SELECT task_name,
                task_parallelism,
                array_agg(max_records_in ::int) as max_throughput,
                avg(max_records_in) ::int       as avg_max_throughput,
                max(max_records_in) :: int      as highest_max_throughput,
                task_position,
                False                           as backpressure_condition_holds

         FROM jobs_to_jar_id j
         WHERE task_name IN (SELECT task_name FROM last_oparator)
         GROUP BY task_name, task_parallelism, task_position
         ORDER BY task_name, task_parallelism),
     non_backpressure_condition_tasks as (
         SELECT j.task_name,
                j.task_parallelism,
                array_agg(j.max_records_out ::int) as max_throughput,
                avg(j.max_records_out) ::int       as avg_max_throughput,
                max(j.max_records_out) :: int      as highest_max_throughput,
                j.task_position,
                False                              as backpressure_condition_holds
         FROM jobs_to_jar_id j
                  LEFT JOIN intermediate_tasks io
                            ON (j.task_name = io.task_name
                                AND j.task_parallelism = io.task_parallelism)
         WHERE io.task_parallelism ISNULL
           AND 0 < j.task_position
           AND j.task_name NOT IN (SELECT task_name FROM last_oparator)
         GROUP BY j.task_name, j.task_parallelism, j.task_position
         ORDER BY task_name, task_parallelism)
SELECT *
FROM intermediate_tasks
UNION ALL
SELECT *
FROM source_task
UNION ALL
SELECT *
FROM sink_task
UNION ALL
SELECT *
FROM non_backpressure_condition_tasks
ORDER BY task_position, task_parallelism
