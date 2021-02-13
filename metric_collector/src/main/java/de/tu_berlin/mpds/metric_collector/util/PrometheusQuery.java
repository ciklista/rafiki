package de.tu_berlin.mpds.metric_collector.util;

import de.tu_berlin.mpds.metric_collector.configuration.ApplicationConfiguration;
import io.mikael.urlbuilder.UrlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class PrometheusQuery {

    private String BASE_URL_PROMETHEUS;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    /**
     * queries: "avg by (operator_id) (flink_taskmanager_job_latency_source_id_operator_id_operator_subtask_index_latency{quantile=\"0.95\"})",
     * queries: ",
     * queries: "avg by (quantile) (flink_taskmanager_job_latency_source_id_operator_id_operator_subtask_index_latency)",
     * queries: "avg by (task_name) (flink_taskmanager_job_task_isBackPressured)"
     **/
    private static final String QUERY_FLINK_JOBMANAGER_STATUS_JVM_CPU_LOAD = "flink_jobmanager_Status_JVM_CPU_Load";

    private static final String QUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD = "flink_taskmanager_Status_JVM_CPU_Load";

    private static final String QUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS = "flink_jobmanager_numRegisteredTaskManagers";

    private static final String QUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS = "flink_jobmanager_numRunningJobs";

    private static final String QUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO = "flink_taskmanager_Status_JVM_Memory_Heap_Used/flink_taskmanager_Status_JVM_Memory_Heap_Committed";

    private static final String QUERY_FLINK_JVM_MEMORY_JOBMANAGER_RATIO = "flink_jobmanager_Status_JVM_Memory_Heap_Used/flink_jobmanager_Status_JVM_Memory_Heap_Committed";

    private static final String QUERY_KAFKA_MESSAGE_IN_PER_SEC = "sum by (topic) (rate(kafka_server_brokertopicmetrics_messagesinpersec_count[2m]))";

    private static final String QUERY_KAFKA_BYTES_IN_PER_SEC = "sum by (topic) (rate(kafka_server_brokertopicmetrics_bytesinpersec_count[2m]))";

    private static final String QUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN = "sum by(job_name) (rate(flink_taskmanager_job_task_numRecordsIn[2m]))";

    private static final String QUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT = "sum by (operator_name) (rate(flink_taskmanager_job_task_operator_numRecordsOut[2m]))";

    private static final String QUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_OPERATOR_ID = "avg by (operator_id) (flink_taskmanager_job_latency_source_id_operator_id_operator_subtask_index_latency)";

    private static final String QUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_QUANTILE = "avg by (quantile) (flink_taskmanager_job_latency_source_id_operator_id_operator_subtask_index_latency)";

    private static final String QUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE = "avg by(task_name)(flink_taskmanager_job_task_isBackPressured)";

    private static final String QUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX = "flink_taskmanager_job_task_operator_KafkaConsumer_records_lag_max";

    private static final String QUERY_MAX_FLINK_TASKMANAGER_JOB_TASK_NUMRECORDSOUTPERSECOND = "max by (task_id) (flink_taskmanager_job_task_numRecordsOutPerSecond)";

    private static final String QUERY_BACKPRESSURE_BY_SUBTASK = " avg by (job_id, task_id, subtask_index) (flink_taskmanager_job_task_isBackPressured) > 0.5";

    public String getBaseUrlPrometheus() {
        BASE_URL_PROMETHEUS = applicationConfiguration.getClusteraddress() + ":" + applicationConfiguration.getPrometheusPort();
        return BASE_URL_PROMETHEUS;
    }

    public String getBaseUrlPrometheusWithPath() {
        BASE_URL_PROMETHEUS = applicationConfiguration.getClusteraddress() + ":" + applicationConfiguration.getPrometheusPort() + applicationConfiguration.getPrometheusapibasepath();
        return BASE_URL_PROMETHEUS;
    }


    public URI getQUERY_FLINK_JOBMANAGER_STATUS_JVM_CPU_LOAD() {

        return build_query(QUERY_FLINK_JOBMANAGER_STATUS_JVM_CPU_LOAD);
    }

    public URI getQUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD() {
        return build_query(QUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD);
    }

    public URI getQUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS() {
        return build_query(QUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS);
    }

    public URI getQUERY_FLINK_JVM_MEMORY_JOBMANAGER_RATIO() {
        return build_query(QUERY_FLINK_JVM_MEMORY_JOBMANAGER_RATIO);
    }

    public URI getQUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS() {
        return build_query(QUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS);
    }

    public URI getQUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO() {
        return build_query(QUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO);
    }


    public URI getQUERY_KAFKA_MESSAGE_IN_PER_SEC() {
        return build_query(QUERY_KAFKA_MESSAGE_IN_PER_SEC);
    }

    public URI getQUERY_KAFKA_BYTES_IN_PER_SEC() {

        return build_query(QUERY_KAFKA_BYTES_IN_PER_SEC);
    }

    public URI getQUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN() {
        return build_query(QUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN);
    }

    public URI getQUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT() {
        return build_query(QUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT);
    }

    public URI getQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_OPERATOR_ID() {
        return build_query(QUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_OPERATOR_ID);
    }

    public URI getQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_QUANTILE() {
        return build_query(QUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_QUANTILE);
    }

    public URI getQUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE() {
        return build_query(QUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE);
    }

    public URI getQUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX() {
        return build_query(QUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX);
    }


    public URI getQUERY_BACKPRESSURE_BY_SUBTASK(ZonedDateTime start, ZonedDateTime end, String step) {
        return build_range_query(QUERY_BACKPRESSURE_BY_SUBTASK, start, end, step);
    }

    public URI getQUERY_MAX_FLINK_TASKMANAGER_JOB_TASK_NUMRECORDSOUTPERSECOND(ZonedDateTime start, ZonedDateTime end, String step) {
        return build_range_query(QUERY_MAX_FLINK_TASKMANAGER_JOB_TASK_NUMRECORDSOUTPERSECOND, start, end, step);
    }

    private URI build_range_query(String queryString, ZonedDateTime start, ZonedDateTime end, String step) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000'Z'");

        return UrlBuilder.fromString(getBaseUrlPrometheus())
                // withPath() will remove the existing path, so we need to add it back
                .withPath(applicationConfiguration.getPrometheusapibasepath() + "query_range")
                .addParameter("query", queryString)
                .addParameter("start", start.format(formatter))
                .addParameter("end", end.format(formatter))
                .addParameter("step", step)
                .toUri();
    }

    private URI build_query(String queryString) {
        return UrlBuilder.fromString(getBaseUrlPrometheus())
                .withPath(applicationConfiguration.getPrometheusapibasepath() + "query")
                .addParameter("query", queryString)
                .toUri();
    }
}
