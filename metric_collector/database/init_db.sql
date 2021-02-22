CREATE SCHEMA IF NOT EXISTS experiments;

CREATE TABLE IF NOT EXISTS experiments.jobs
(
    job_id   TEXT,
    job_name TEXT,
    PRIMARY KEY (job_id)
);

CREATE TABLE IF NOT EXISTS experiments.operators
(
    operator_id            TEXT, -- is task id
    job_id                 TEXT,
    task_name              TEXT,
    preceding_operator_id  TEXT,
    succeeding_operator_id TEXT,
    PRIMARY KEY (operator_id, job_id),
    FOREIGN KEY (job_id) REFERENCES experiments.jobs (job_id)


);

CREATE TABLE IF NOT EXISTS experiments.results
(
    experiment_id                       TEXT,
    job_id                              TEXT,
    start_timestamp                     BIGINT,
    end_timestamp                       BIGINT,
    PRIMARY KEY (experiment_id),
     FOREIGN KEY (job_id) REFERENCES experiments.jobs(job_id)

);

CREATE TABLE IF NOT EXISTS experiments.kafka_metrics
(
    experiment_id                 TEXT,
    max_kafka_lag                 NUMERIC,
    mac_kafka_messages_per_second NUMERIC,
    FOREIGN KEY (experiment_id) REFERENCES experiments.results (experiment_id)

);

CREATE TABLE IF NOT EXISTS experiments.operator_metrics
(
    experiment_id        TEXT,
    operator_id          TEXT,
    job_id               TEXT,
    operator_parallelism INT,
    max_records_in       NUMERIC,
    max_records_out      NUMERIC,
    max_bytes_in         NUMERIC,
    max_bytes_out        NUMERIC,
    max_latency          NUMERIC,
    max_backpresure      NUMERIC,
    FOREIGN KEY (experiment_id) REFERENCES experiments.results (experiment_id),
    FOREIGN KEY (operator_id, job_id) REFERENCES experiments.operators (operator_id, job_id)


);
