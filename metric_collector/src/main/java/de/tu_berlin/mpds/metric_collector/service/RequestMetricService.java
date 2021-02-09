package de.tu_berlin.mpds.metric_collector.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.model.flinkmetric.Job;
import de.tu_berlin.mpds.metric_collector.model.flinkmetric.JobVertex;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.PrometheusJsonResponse;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.Result;
import de.tu_berlin.mpds.metric_collector.util.FlinkQuery;
import de.tu_berlin.mpds.metric_collector.configuration.ApplicationConfiguration;
import de.tu_berlin.mpds.metric_collector.util.PrometheusQuery;
import java.math.BigInteger;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutionException;

@Service
public class RequestMetricService {


  private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
  private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);;
  @Autowired
  private FlinkAPIMetricService flinkAPIMetricService;
  @Autowired
  private PrometheusMetricService prometheusMetricService;
  @Autowired
  private PrometheusQuery prometheusQuery;
  @Autowired
  private FlinkQuery flinkQuery;


  //@PostConstruct
  @Scheduled(cron = "0/2 * * * * ?")
  public void init() throws InterruptedException, ExecutionException, IOException {
    System.out.println("---- PROMETHEUS QUERIES -----");


    try {
      PrometheusJsonResponse responseFlinkTaskManagerJVMCPULoad = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD(), client, objectMapper);
      List<Result> taskCPUQueryResults = responseFlinkTaskManagerJVMCPULoad.getData().getResult();
      PrometheusJsonResponse responseFlinkJVMMemoryTaskManagerRatio = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO(), client, objectMapper);
      List<Result> taskMemoryQueryResults = responseFlinkJVMMemoryTaskManagerRatio.getData().getResult();

      System.out.println("Found " + taskCPUQueryResults.size() + " Task Manager(s)");
      printQueryResults(taskCPUQueryResults, taskMemoryQueryResults);

      PrometheusJsonResponse responseFlinkJobManagerJVMCPULoad = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_JOBMANAGER_STATUS_JVM_CPU_LOAD(), client, objectMapper);
      List<Result> jobCPUQueryResults = responseFlinkJobManagerJVMCPULoad.getData().getResult();
      PrometheusJsonResponse responseFlinkJVMMemoryJobManagerRatio = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_JVM_MEMORY_JOBMANAGER_RATIO(), client, objectMapper);
      List<Result> jobMemoryQueryResults = responseFlinkJVMMemoryJobManagerRatio.getData().getResult();
      System.out.println("----");
      System.out.println("Found " + jobCPUQueryResults.size() + " Job Manager(s)");
      printQueryResults(jobCPUQueryResults, jobMemoryQueryResults);

      PrometheusJsonResponse responseFlinkNumOfRunningJobs = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS(), client, objectMapper);
      System.out.println("Number of running jobs: " + responseFlinkNumOfRunningJobs.getData().getResult().get(0).getValue().get(1));

      PrometheusJsonResponse responseFlinkNumOfRunningTaskManager = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS(), client, objectMapper);
      System.out.println("Number of running TaskManagers: " + responseFlinkNumOfRunningTaskManager.getData().getResult().get(0).getValue().get(1));

      PrometheusJsonResponse responseQUERY_KAFKA_MESSAGE_IN_PER_SEC = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_KAFKA_MESSAGE_IN_PER_SEC(), client, objectMapper);
      System.out.println("QUERY_KAFKA_MESSAGE_IN_PER_SEC: " + responseQUERY_KAFKA_MESSAGE_IN_PER_SEC.getData().getResult().get(0).getValue().get(1));

      PrometheusJsonResponse responseQUERY_KAFKA_BYTES_IN_PER_SEC = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_KAFKA_BYTES_IN_PER_SEC(), client, objectMapper);
      System.out.println("QUERY_KAFKA_BYTES_IN_PER_SEC: " + responseQUERY_KAFKA_BYTES_IN_PER_SEC.getData().getResult().get(0).getValue().get(1));

      PrometheusJsonResponse responseQUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN(), client, objectMapper);
      System.out.println("QUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN: " + responseQUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN.getData().getResult().get(0).getValue().get(1));

      PrometheusJsonResponse responseQUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT(), client, objectMapper);
      System.out.println("QUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT: " + responseQUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT.getData().getResult().get(0).getValue().get(1));

      PrometheusJsonResponse responseQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_OPERATOR_ID = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_OPERATOR_ID(), client, objectMapper);
      System.out.println("QUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_OPERATOR_ID: " + responseQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_OPERATOR_ID.getData().getResult().get(0).getValue().get(1));

      PrometheusJsonResponse responseQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_QUANTILE = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_QUANTILE(), client, objectMapper);
      System.out.println("QUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_QUANTILE: " + responseQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_QUANTILE.getData().getResult().get(0).getValue().get(1));

      PrometheusJsonResponse responseQUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE(), client, objectMapper);
      System.out.println("QUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE: " + responseQUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE.getData().getResult().get(0).getValue().get(1));

      PrometheusJsonResponse responseQUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX(), client, objectMapper);
      System.out.println("QUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX: " + responseQUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX.getData().getResult().get(0).getValue().get(1));


      System.out.println("---- FLINK API -----");
      List<Job> jobs = flinkAPIMetricService.getJobs(flinkQuery.getFLINK_JOBS_OVERVIEW(), client, objectMapper);
      System.out.println("Received " + jobs.size() + " job(s):");

      for (Job job : jobs) {
        System.out.println("Job " + job.getJid() + ": " + job.getName() + " (" + job.getState() + ")");
        Job job_info = flinkAPIMetricService.getJobInfo(flinkQuery.getFLINK_JOBS() + job.getJid(), client, objectMapper);
        System.out.println("The job has the following vertices: ");
        BigInteger prev_records_out = new BigInteger("-1");
        BigInteger conditionBigInt = BigInteger.ZERO;
        for (JobVertex vertex : job_info.getVertices()) {
          System.out.println("Name: " + vertex.getName());
          System.out.println("Parallelism: " + vertex.getParallelism());
          System.out.println("Status: " + vertex.getStatus());
          System.out.println("Number of incoming records: " + vertex.getMetrics().getRead_records());
          System.out.println("Number of outgoing records: " + vertex.getMetrics().getWrite_records());
          if (prev_records_out.compareTo(conditionBigInt) >= 0) {
            BigInteger lag = prev_records_out.subtract(vertex.getMetrics().getRead_records()) ;
            System.out.println("->> Vertex is lagging behind by " + lag + " records");
          }
          prev_records_out = vertex.getMetrics().getWrite_records();
          System.out.println("---");
        }
        System.out.println("------");

      }

    } catch (NullPointerException e ) {
      System.out.println("Null pointer exception");
    }
    catch (IndexOutOfBoundsException e ) {
      System.out.println("IndexOutOfBoundsException ");
    }
  }

  private void printQueryResults(List<Result> jobCPUQueryResults, List<Result> jobMemoryQueryResults) {
    for (int i = 0; i < jobCPUQueryResults.size(); i++) {
      System.out.println("Pod name: " + jobCPUQueryResults.get(i).getMetric().getKubernetesPodName());
      System.out.println("Memory load: " + jobMemoryQueryResults.get(i).getValue().get(1));
      System.out.println("CPU load: " + jobCPUQueryResults.get(i).getValue().get(1));
      System.out.println("----");
    }
  }
}
