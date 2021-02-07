package de.tu_berlin.mpds.metric_collector.configuration;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Getter
public class PrometheusConfig extends ClusterConfig{

  //we will change this configuration class by mapping it to properties file.
  public static final String BASE_URL_PROMETHEUS = "http://" + CLUSTER_URL + ":30090/api/v1/";

  public static final String QUERY_FLINK_JOBMANAGER_STATUS_JVM_CPU_LOAD = "query?query=flink_jobmanager_Status_JVM_CPU_Load";
  public static final String QUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD = "query?query=flink_taskmanager_Status_JVM_CPU_Load";

  public static final String QUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS = "query?query=flink_jobmanager_numRegisteredTaskManagers";
  public static final String QUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS = "query?query=flink_jobmanager_numRunningJobs";

  public static final String QUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO = "query?query=flink_taskmanager_Status_JVM_Memory_Heap_Used/flink_taskmanager_Status_JVM_Memory_Heap_Committed";
  public static final String QUERY_FLINK_JVM_MEMORY_JOBMANAGER_RATIO = "query?query=flink_jobmanager_Status_JVM_Memory_Heap_Used/flink_jobmanager_Status_JVM_Memory_Heap_Committed";


}
