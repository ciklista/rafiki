CREATE SCHEMA IF NOT EXISTS experiments;

CREATE TABLE IF NOT EXISTS experiments.jobs
(
    job_id   TEXT,
    job_name TEXT,
    jar_id   TEXT,
    PRIMARY KEY (job_id)
);

CREATE TABLE IF NOT EXISTS experiments.tasks
(
    task_id                TEXT,
    job_id                 TEXT,
    task_name              TEXT,
    task_position      INT,
    PRIMARY KEY (task_id, job_id),
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

CREATE TABLE IF NOT EXISTS experiments.metrics
(
    experiment_id        TEXT,
    task_id              TEXT,
    job_id               TEXT,
    task_parallelism     INT,
    max_records_in       NUMERIC,
    max_records_out      NUMERIC,
    max_bytes_in         NUMERIC,
    max_bytes_out        NUMERIC,
    max_latency          NUMERIC,
    min_latency          NUMERIC,
    max_backpresure      NUMERIC,
    FOREIGN KEY (experiment_id) REFERENCES experiments.results (experiment_id),
    FOREIGN KEY (task_id, job_id) REFERENCES experiments.tasks (task_id, job_id)


);
