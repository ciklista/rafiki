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


    private  String QUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD = "flink_taskmanager_Status_JVM_CPU_Load";

    private  String QUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS = "flink_jobmanager_numRegisteredTaskManagers";

    private  String QUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS = "flink_jobmanager_numRunningJobs";

    private  String QUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO = "flink_taskmanager_Status_JVM_Memory_Heap_Used/flink_taskmanager_Status_JVM_Memory_Heap_Committed";

    private String QUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX = "flink_taskmanager_job_task_operator_KafkaConsumer_records_lag_max";

    private String QUERY_MAX_FLINK_TASKMANAGER_JOB_TASK_NUMRECORDSOUTPERSECOND = "max+by+(task_id)+(flink_taskmanager_job_task_numRecordsOutPerSecond)";

    public URI getQUERY_BYTES_IN_BY_TASK(String job_id) {
        return build_query( "sum by (job_id, task_id) (flink_taskmanager_job_task_numBytesInPerSecond {job_id = \"" + job_id + "\"})");
    }

    public URI getQUERY_BYTES_OUT_BY_TASK(String job_id) {
        return build_query( "sum by (job_id, task_id) (flink_taskmanager_job_task_numBytesOutPerSecond {job_id = \"" + job_id + "\"})");
    }

    public URI getQUERY_MESSAGES_IN_BY_TASK(String job_id) {
        return build_query( "sum by (job_id, task_id) (flink_taskmanager_job_task_numRecordsInPerSecond {job_id = \"" + job_id + "\"})");
    }

    public URI getQUERY_MESSAGES_OUT_BY_TASK(String job_id) {
        return build_query( "sum by (job_id, task_id) (flink_taskmanager_job_task_numRecordsOutPerSecond {job_id = \"" + job_id + "\"})");
    }

    public URI getQUERY_MAX_BACKPRESSURE_BY_TASK (String job_id){
        return build_query( "max by (job_id, task_id) (flink_taskmanager_job_task_isBackPressured {job_id = \"" + job_id + "\"})");
    }

    public URI getQUERY_AVG_LATENCY_BY_TASK (String job_id){
        return build_query( "avg by (operator_id) (flink_taskmanager_job_latency_source_id_operator_id_operator_subtask_index_latency{quantile=\"0.95\", job_id = \"" + job_id + "\"})");
    }

    public URI getQUERY_KAFKA_MESSAGE_IN_PER_SEC() {
        return build_query("sum by (topic) (kafka_server_brokertopicmetrics_messagesinpersec_count{topic=\"ad-events\"})");
    }

    public URI getQUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX(String job_Id) {
        return build_query("sum by (operator_id) (flink_taskmanager_job_task_operator_KafkaConsumer_records_lag_max{job_id=\"" + job_Id +"\"})" );
    }


    //flink_taskmanager_job_task_operator_KafkaConsumer_records_consumed_rate





    // keep or remove
    public URI getQUERY_KAFKA_BYTES_IN_PER_SEC(String topic) {
        String QUERY_KAFKA_BYTES_IN_PER_SEC = "sum+by+"+(topic)+"+(rate(kafka_server_brokertopicmetrics_bytesinpersec_count[2m]))";

        return build_query(QUERY_KAFKA_BYTES_IN_PER_SEC);
    }


    public URI getQUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN(String taskID) {
        String QUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN = "sum+by"+(taskID)+"+(rate(flink_taskmanager_job_task_numRecordsIn[2m]))";
        return build_query(QUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN);
    }


    public URI getQUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT(String operator_name) {
        String QUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT = "sum+by"+(operator_name)+"rate(flink_taskmanager_job_task_operator_numRecordsOut[2m])";
        return build_query(QUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT);
    }


    public URI getQUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE(String taskID ) {
        String QUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE = "avg+by"+(taskID)+"flink_taskmanager_job_task_isBackPressured";
        return build_query(QUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE);
    }

    public URI getflink_taskmanager_job_latency(String operator_id ) {
        String flink_taskmanager_job_latency = "avg+by"+(operator_id)+"(flink_taskmanager_job_latency_source_id_operator_id_operator_subtask_index_latency+%7B+quantile+%3D+%22+0.95+%22+%7D+)";
        return build_query(flink_taskmanager_job_latency);
    }

    public String getBaseUrlPrometheus() {
        BASE_URL_PROMETHEUS = applicationConfiguration.getClusteraddress() + ":" + applicationConfiguration.getPrometheusPort();
        return BASE_URL_PROMETHEUS;
    }

    public String getBaseUrlPrometheusWithPath() {
        BASE_URL_PROMETHEUS = applicationConfiguration.getClusteraddress() + ":" + applicationConfiguration.getPrometheusPort() + applicationConfiguration.getPrometheusapibasepath();
        return BASE_URL_PROMETHEUS;
    }

    public URI getQUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD() {
        return build_query(QUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD);
    }

    public URI getQUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS() {
        return build_query(QUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS);
    }



    public URI getQUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS() {
        return build_query(QUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS);
    }

    public URI getQUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO() {
        return build_query(QUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO);
    }




   // public URI getQUERY_BACKPRESSURE_BY_SUBTASK(ZonedDateTime start, ZonedDateTime end, String step) {return build_range_query(QUERY_BACKPRESSURE_BY_SUBTASK, start, end, step);}

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
