package de.tu_berlin.mpds.metric_collector.util;

import de.tu_berlin.mpds.metric_collector.configuration.ApplicationConfiguration;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrometheusQuery {

  private String BASE_URL_PROMETHEUS;
  @Autowired
  private ApplicationConfiguration applicationConfiguration;
/**


  queries: "avg by (operator_id) (flink_taskmanager_job_latency_source_id_operator_id_operator_subtask_index_latency{quantile=\"0.95\"})",
  queries: ",
  queries: "avg by (quantile) (flink_taskmanager_job_latency_source_id_operator_id_operator_subtask_index_latency)",
  queries: "avg by (task_name) (flink_taskmanager_job_task_isBackPressured)"

 **/
  private static final String QUERY_FLINK_JOBMANAGER_STATUS_JVM_CPU_LOAD = "query?query=flink_jobmanager_Status_JVM_CPU_Load";

  private static final String QUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD = "query?query=flink_taskmanager_Status_JVM_CPU_Load";

  private static final String QUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS = "query?query=flink_jobmanager_numRegisteredTaskManagers";

  private static final String QUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS = "query?query=flink_jobmanager_numRunningJobs";

  private static final String QUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO = "query?query=flink_taskmanager_Status_JVM_Memory_Heap_Used/flink_taskmanager_Status_JVM_Memory_Heap_Committed";

  private static final String QUERY_FLINK_JVM_MEMORY_JOBMANAGER_RATIO = "query?query=flink_jobmanager_Status_JVM_Memory_Heap_Used/flink_jobmanager_Status_JVM_Memory_Heap_Committed";

  private static final String QUERY_KAFKA_MESSAGE_IN_PER_SEC = "query?query=sum+by(topic)(rate(kafka_server_brokertopicmetrics_messagesinpersec_count[2m]))";

  private static final String QUERY_KAFKA_BYTES_IN_PER_SEC = "query?query=sum+by(topic)(rate(kafka_server_brokertopicmetrics_bytesinpersec_count[2m]))";

  private static final String QUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN = "query?query=sum+by(job_name)(rate(flink_taskmanager_job_task_numRecordsIn[2m]))";

  private static final String QUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT = "query?query=sum+by(operator_name)(rate(flink_taskmanager_job_task_operator_numRecordsOut[2m]))";

  private static final String QUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_OPERATOR_ID = "query?query=avg+by(operator_id)(flink_taskmanager_job_latency_source_id_operator_id_operator_subtask_index_latency)";

  private static final String QUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_QUANTILE = "query?query=avg+by(quantile)(flink_taskmanager_job_latency_source_id_operator_id_operator_subtask_index_latency)";

  private static final String QUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE = "query?query=avg+by(task_name)(flink_taskmanager_job_task_isBackPressured)";

  private static final String QUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX = "query?query=flink_taskmanager_job_task_operator_KafkaConsumer_records_lag_max";

  private static final String QUERY_MAX_FLINK_TASKMANAGER_JOB_TASK_NUMRECORDSOUTPERSECOND = "max by (task_id) (flink_taskmanager_job_task_numRecordsOutPerSecond)";

  public String getBaseUrlPrometheus() {
    BASE_URL_PROMETHEUS = applicationConfiguration.getClusteraddress() + ":" + applicationConfiguration.getPrometheusPort() + applicationConfiguration.getPrometheusapibasepath();
    return BASE_URL_PROMETHEUS;
  }


  public String getQUERY_FLINK_JOBMANAGER_STATUS_JVM_CPU_LOAD() {

    return getBaseUrlPrometheus() + QUERY_FLINK_JOBMANAGER_STATUS_JVM_CPU_LOAD;
  }

  public String getQUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD() {
    return getBaseUrlPrometheus() + QUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD;
  }

  public String getQUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS() {
    return getBaseUrlPrometheus() + QUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS;
  }

  public String getQUERY_FLINK_JVM_MEMORY_JOBMANAGER_RATIO() {
    return getBaseUrlPrometheus() + QUERY_FLINK_JVM_MEMORY_JOBMANAGER_RATIO;
  }
  public String getQUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS() {
    return getBaseUrlPrometheus() + QUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS;
  }

  public String getQUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO() {
    return getBaseUrlPrometheus() + QUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO;
  }

  public String getQUERY_MAX_FLINK_TASKMANAGER_JOB_TASK_NUMRECORDSOUTPERSECOND() {
    return QUERY_MAX_FLINK_TASKMANAGER_JOB_TASK_NUMRECORDSOUTPERSECOND;
  }

  public  String getQUERY_KAFKA_MESSAGE_IN_PER_SEC() {
    return getBaseUrlPrometheus() + QUERY_KAFKA_MESSAGE_IN_PER_SEC;
  }

  public  String getQUERY_KAFKA_BYTES_IN_PER_SEC() {
    return getBaseUrlPrometheus() + QUERY_KAFKA_BYTES_IN_PER_SEC;
  }

  public  String getQUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN() {
    return getBaseUrlPrometheus() + QUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN;
  }

  public  String getQUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT() {
    return getBaseUrlPrometheus() +  QUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT;
  }

  public  String getQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_OPERATOR_ID() {
    return getBaseUrlPrometheus() + QUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_OPERATOR_ID;
  }

  public  String getQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_QUANTILE() {
    return getBaseUrlPrometheus() + QUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_QUANTILE;
  }

  public String getQUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE() {
    return getBaseUrlPrometheus() + QUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE;
  }

  public  String getQUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX() {
    return getBaseUrlPrometheus() + QUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX;
  }
}
