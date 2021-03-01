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

    private String getBaseUrlPrometheus(String clusterAddress) {
        BASE_URL_PROMETHEUS = clusterAddress + ":" + applicationConfiguration.getPrometheusPort();
        return BASE_URL_PROMETHEUS;
    }

    private String getBaseUrlPrometheusWithPath(String clusterAddress) {
        BASE_URL_PROMETHEUS = clusterAddress + ":" + applicationConfiguration.getPrometheusPort() + applicationConfiguration.getPrometheusapibasepath();
        return BASE_URL_PROMETHEUS;
    }

    public URI getBytesInByTask(String clusterAddress, String job_id) {
        return build_query(clusterAddress, "sum by (job_id, task_id) (flink_taskmanager_job_task_numBytesInPerSecond {job_id = \"" + job_id + "\"})");
    }

    public URI getBytesOutByTask(String clusterAddress, String job_id) {
        return build_query(clusterAddress, "sum by (job_id, task_id) (flink_taskmanager_job_task_numBytesOutPerSecond {job_id = \"" + job_id + "\"})");
    }

    public URI getMessagesInByTask(String clusterAddress, String job_id) {
        return build_query(clusterAddress, "sum by (job_id, task_id) (flink_taskmanager_job_task_numRecordsInPerSecond {job_id = \"" + job_id + "\"})");
    }

    public URI getMessagesOutByTask(String clusterAddress, String job_id) {
        return build_query(clusterAddress, "sum by (job_id, task_id) (flink_taskmanager_job_task_numRecordsOutPerSecond {job_id = \"" + job_id + "\"})");
    }

    public URI getMaxBackpressureByTask(String clusterAddress, String job_id) {
        return build_query(clusterAddress, "max by (job_id, task_id) (flink_taskmanager_job_task_isBackPressured {job_id = \"" + job_id + "\"})");
    }

    public URI getAvgLatencyByTask(String clusterAddress, String job_id) {
        return build_query(clusterAddress, "avg by (operator_id) (flink_taskmanager_job_latency_source_id_operator_id_operator_subtask_index_latency{quantile=\"0.95\", job_id = \"" + job_id + "\"})");
    }

    public URI getKafkaMessagesInPerSecond(String clusterAddress) {
        return build_query(clusterAddress, "sum by (topic) (kafka_server_brokertopicmetrics_messagesinpersec_count{topic=\"ad-events\"})");
    }

    public URI getTaskmangerKafakaconsumerRecordLag(String clusterAddress, String job_Id) {
        return build_query(clusterAddress, "sum by (operator_id) (flink_taskmanager_job_task_operator_KafkaConsumer_records_lag_max{job_id=\"" + job_Id + "\"})");
    }


    // keep or remove
    public URI getKafakBytesInPerSecond(String clusterAddress, String topic) {
        String QUERY_KAFKA_BYTES_IN_PER_SEC = "sum+by+" + (topic) + "+(rate(kafka_server_brokertopicmetrics_bytesinpersec_count[2m]))";
        return build_query(clusterAddress, QUERY_KAFKA_BYTES_IN_PER_SEC);
    }


    public URI getTaskmangerJvmCpuLoad(String clusterAddress) {
        String QUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD = "flink_taskmanager_Status_JVM_CPU_Load";
        return build_query(clusterAddress, QUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD);
    }

    public URI getNumRegisteredTaskManagers(String clusterAddress) {
        String QUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS = "flink_jobmanager_numRegisteredTaskManagers";
        return build_query(clusterAddress, QUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS);
    }


    public URI getNumRunningJobs(String clusterAddress) {
        String QUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS = "flink_jobmanager_numRunningJobs";
        return build_query(clusterAddress, QUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS);
    }

    public URI getTaskmanagerJvmMemoryRatio(String clusterAddress) {
        String QUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO = "flink_taskmanager_Status_JVM_Memory_Heap_Used/flink_taskmanager_Status_JVM_Memory_Heap_Committed";
        return build_query(clusterAddress, QUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO);
    }

    public URI getMessagesOutByTask(String clusterAddress, ZonedDateTime start, ZonedDateTime end, String step) {
        String QUERY_MAX_FLINK_TASKMANAGER_JOB_TASK_NUMRECORDSOUTPERSECOND = "max+by+(task_id)+(flink_taskmanager_job_task_numRecordsOutPerSecond)";
        return build_range_query(clusterAddress, QUERY_MAX_FLINK_TASKMANAGER_JOB_TASK_NUMRECORDSOUTPERSECOND, start, end, step);
    }

    private URI build_range_query(String clusterAddress, String queryString, ZonedDateTime start, ZonedDateTime end, String step) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000'Z'");

        return UrlBuilder.fromString(getBaseUrlPrometheus(clusterAddress))
                // withPath() will remove the existing path, so we need to add it back
                .withPath(applicationConfiguration.getPrometheusapibasepath() + "query_range")
                .addParameter("query", queryString)
                .addParameter("start", start.format(formatter))
                .addParameter("end", end.format(formatter))
                .addParameter("step", step)
                .toUri();
    }

    private URI build_query(String clusterAddress, String queryString) {
        return UrlBuilder.fromString(getBaseUrlPrometheus(clusterAddress))
                .withPath(applicationConfiguration.getPrometheusapibasepath() + "query")
                .addParameter("query", queryString)
                .toUri();
    }

}
