WITH all_jobs AS (SELECT job_id, job_name
                  FROM experiments.jobs
                  WHERE jar_id = ?),
     all_operators AS (
         SELECT max(task_name)         as task_name,
                max(job_name)          as job_name,
                max(operator_id)       as operator_id,
                max(operator_position) as operator_position
         FROM experiments.operators o
                  LEFT JOIN all_jobs j ON o.job_id = j.job_id
         GROUP BY operator_id),
     add_previous_operator AS (
         SELECT *,
                LAG(operator_id, 1) OVER (
                    ORDER BY operator_position
                    ) previous_operator_id
         FROM all_operators),
     metrics AS (
         SELECT o.task_name,
                o.job_name,
                om1.job_id,
                om1.operator_parallelism,
                om1.max_records_out,
                om1.max_records_in,
                om1.experiment_id,
                om1.max_backpresure,
                om2.max_backpresure as previous_operator_backpressure,
                operator_position
         FROM add_previous_operator o
                  LEFT JOIN experiments.operator_metrics om1
                            ON (o.operator_id = om1.operator_id)
                  LEFT JOIN experiments.operator_metrics om2
                            ON (o.previous_operator_id = om2.operator_id AND
                                om1.experiment_id = om2.experiment_id)),
     jobs_to_jar_id AS (
         SELECT *
         FROM metrics m
                  lEFT JOIN all_jobs j ON (m.job_id = j.job_id)
         WHERE j.job_id NOTNULL),
     last_oparator AS (SELECT task_name
                       FROM all_operators
                       ORDER BY operator_position DESC
                       FETCH FIRST 1 ROW ONLY
     ),
     intermediate_operators as (
         SELECT task_name,
                operator_parallelism,
                array_agg(max_records_out ::int) as max_throughput,
                avg(max_records_out) ::int       as avg_max_throughput,
                max(max_records_out) :: int      as highest_max_throughput,
                operator_position,
                True                             as backpressure_condition_holds
         FROM jobs_to_jar_id j
         WHERE previous_operator_backpressure > 0.5
           AND max_backpresure < 0.5
           AND 0 < operator_position
           AND task_name NOT IN (SELECT task_name FROM last_oparator)
         GROUP BY task_name, operator_parallelism, operator_position
         ORDER BY task_name, operator_parallelism),
     source_operator as (
         SELECT task_name,
                operator_parallelism,
                array_agg(max_records_out ::int) as max_throughput,
                avg(max_records_out) ::int       as avg_max_throughput,
                max(max_records_out) :: int      as highest_max_throughput,
                operator_position,
                True                             as backpressure_condition_holds

         FROM jobs_to_jar_id j
         WHERE operator_position = 0
         GROUP BY task_name, operator_parallelism, operator_position
         ORDER BY task_name, operator_parallelism),

     sink_operator as (
         SELECT task_name,
                operator_parallelism,
                array_agg(max_records_in ::int) as max_throughput,
                avg(max_records_in) ::int       as avg_max_throughput,
                max(max_records_in) :: int      as highest_max_throughput,
                operator_position,
                False                           as backpressure_condition_holds

         FROM jobs_to_jar_id j
         WHERE task_name IN (SELECT task_name FROM last_oparator)
         GROUP BY task_name, operator_parallelism, operator_position
         ORDER BY task_name, operator_parallelism),
     non_backpressure_condition_operators as (
         SELECT j.task_name,
                j.operator_parallelism,
                array_agg(j.max_records_out ::int) as max_throughput,
                avg(j.max_records_out) ::int       as avg_max_throughput,
                max(j.max_records_out) :: int      as highest_max_throughput,
                j.operator_position,
                False                              as backpressure_condition_holds
         FROM jobs_to_jar_id j
                  LEFT JOIN intermediate_operators io
                            ON (j.task_name = io.task_name
                                AND j.operator_parallelism = io.operator_parallelism)
         WHERE io.operator_parallelism ISNULL
           AND 0 < j.operator_position
           AND j.task_name NOT IN (SELECT task_name FROM last_oparator)
         GROUP BY j.task_name, j.operator_parallelism, j.operator_position
         ORDER BY task_name, operator_parallelism)
SELECT *
FROM intermediate_operators
UNION ALL
SELECT *
FROM source_operator
UNION ALL
SELECT *
FROM sink_operator
UNION ALL
SELECT *
FROM non_backpressure_condition_operators
ORDER BY operator_position, operator_parallelism