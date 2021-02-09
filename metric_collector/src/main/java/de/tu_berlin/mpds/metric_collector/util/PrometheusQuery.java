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

  private static final String QUERY_FLINK_JOBMANAGER_STATUS_JVM_CPU_LOAD = "query?query=flink_jobmanager_Status_JVM_CPU_Load";

  private static final String QUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD = "query?query=flink_taskmanager_Status_JVM_CPU_Load";

  private static final String QUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS = "query?query=flink_jobmanager_numRegisteredTaskManagers";

  private static final String QUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS = "query?query=flink_jobmanager_numRunningJobs";

  private static final String QUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO = "query?query=flink_taskmanager_Status_JVM_Memory_Heap_Used/flink_taskmanager_Status_JVM_Memory_Heap_Committed";

  private static final String QUERY_FLINK_JVM_MEMORY_JOBMANAGER_RATIO = "query?query=flink_jobmanager_Status_JVM_Memory_Heap_Used/flink_jobmanager_Status_JVM_Memory_Heap_Committed";

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

}
