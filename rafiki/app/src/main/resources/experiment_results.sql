WITH all_tasks AS (
    SELECT t.task_name   as task_name,
           job_name      as job_name,
           j.job_id      as job_id,
           task_id       as task_id,
           task_position as task_position,
           j.jar_id      as jar_id
    FROM experiments.tasks t
             LEFT JOIN experiments.jobs j ON t.job_id = j.job_id
    WHERE j.job_id NOTNULL
    GROUP BY 1, 2, 3, 4, 5, 6),
     add_previous_tasks AS (
         SELECT *,
                LAG(task_id, 1) OVER ( PARTITION BY job_name,job_id
                    ORDER BY task_position
                    ) previous_task_id
         FROM all_tasks),
     metrics AS (
         SELECT t.task_name,
                t.job_name,
                t.jar_id,
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
                            ON (t.task_id = om1.task_id AND t.job_id = om1.job_id)
                  LEFT JOIN experiments.metrics om2
                            ON (t.previous_task_id = om2.task_id AND
                                om1.experiment_id = om2.experiment_id AND t.job_id = om1.job_id)),
     highest_task AS (SELECT max(task_position) as max_position, job_name
                      FROM all_tasks
                      GROUP BY job_name
     ),
     intermediate_tasks as (
         SELECT jar_id,
                task_name,
                task_parallelism,
                array_agg(max_records_out ::int) as max_throughput,
                avg(max_records_out) ::int       as avg_max_throughput,
                max(max_records_out) :: int      as highest_max_throughput,
                task_position,
                True                             as backpressure_condition_holds
         FROM metrics m
                  LEFT JOIN highest_task h ON (m.job_name = h.job_name)
         WHERE previous_task_backpressure > 0.5
           AND max_backpresure < 0.5
           AND task_position > 0
           AND task_position != h.max_position
         GROUP BY jar_id, task_name, task_parallelism, task_position
         ORDER BY jar_id, task_name, task_parallelism),
     source_task as (
         SELECT jar_id,
                task_name,
                task_parallelism,
                array_agg(max_records_out ::int) as max_throughput,
                avg(max_records_out) ::int       as avg_max_throughput,
                max(max_records_out) :: int      as highest_max_throughput,
                task_position,
                True                             as backpressure_condition_holds
         FROM metrics m
         WHERE task_position = 0
         GROUP BY jar_id, task_name, task_parallelism, task_position
         ORDER BY jar_id, task_name, task_parallelism),

     sink_task as (
         SELECT jar_id,
                task_name,
                task_parallelism,
                array_agg(max_records_in ::int) as max_throughput,
                avg(max_records_in) ::int       as avg_max_throughput,
                max(max_records_in) :: int      as highest_max_throughput,
                task_position,
                False                           as backpressure_condition_holds

         FROM metrics m
                  LEFT JOIN highest_task h ON (m.job_name = h.job_name)
         WHERE task_position = h.max_position
         GROUP BY jar_id, task_name, task_parallelism, task_position
         ORDER BY jar_id, task_name, task_parallelism),
     non_backpressure_condition_tasks as (
         SELECT m.jar_id,
                m.task_name,
                m.task_parallelism,
                array_agg(m.max_records_out ::int) as max_throughput,
                avg(m.max_records_out) ::int       as avg_max_throughput,
                max(m.max_records_out) :: int      as highest_max_throughput,
                m.task_position,
                False                              as backpressure_condition_holds
         FROM metrics m
                  LEFT JOIN intermediate_tasks io
                            ON (m.task_name = io.task_name
                                AND m.task_parallelism = io.task_parallelism)
                  LEFT JOIN highest_task h ON (m.job_name = h.job_name)
         WHERE io.task_parallelism ISNULL
           AND 0 < m.task_position
           AND m.task_position != h.max_position
         GROUP BY m.jar_id, m.task_name, m.task_parallelism, m.task_position
         ORDER BY m.jar_id, task_name, task_parallelism),
     base AS (
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
         FROM non_backpressure_condition_tasks)
SELECT task_name,
       task_parallelism,
       max_throughput,
       avg_max_throughput,
       highest_max_throughput,
       task_position,
       backpressure_condition_holds
FROM base
WHERE jar_id = ?
ORDER BY task_position, task_parallelism
;
