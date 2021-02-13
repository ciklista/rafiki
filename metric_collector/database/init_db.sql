CREATE SCHEMA IF NOT EXISTS experiments;

CREATE TABLE IF NOT EXISTS experiments.jobs
(
    job_id   INT,
    job_name TEXT,
    PRIMARY KEY (job_id)
);

CREATE TABLE IF NOT EXISTS experiments.operators
(
    operator_id            INT, -- is task id
    job_id                 INT,
    task_name              TEXT,
    preceding_operator_id  INT,
    succeeding_operator_id INT,
    PRIMARY KEY (operator_id),
    FOREIGN KEY (job_id) REFERENCES experiments.jobs (job_id)


);

CREATE TABLE IF NOT EXISTS experiments.results
(
    experiment_id                       SERIAL,
    job_id                              INT,
    start_timestamp                     TIMESTAMPTZ,
    end_timestamp                       TIMESTAMPTZ,
    PRIMARY KEY (experiment_id),
     FOREIGN KEY (job_id) REFERENCES experiments.jobs(job_id)

);

CREATE TABLE IF NOT EXISTS experiments.kafka_metrics
(
    experiment_id                 SERIAL,
    max_kafka_lag                 NUMERIC,
    mac_kafka_messages_per_second NUMERIC,
    FOREIGN KEY (experiment_id) REFERENCES experiments.results (experiment_id)

);

CREATE TABLE IF NOT EXISTS experiments.operator_metrics
(
    experiment_id        SERIAL,
    operator_id          INT,
    operator_parallelism INT,
    max_records_in       INT,
    max_records_out      INT,
    max_bytes_in         INT,
    max_bytes_out        INT,
    max_latency          NUMERIC,
    max_backpresure      NUMERIC,
    FOREIGN KEY (experiment_id) REFERENCES experiments.results (experiment_id),
    FOREIGN KEY (operator_id) REFERENCES experiments.operators (operator_id)


);
