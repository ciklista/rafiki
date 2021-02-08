package de.tu_berlin.mpds.metric_collector.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.model.flinkmetric.Job;
import de.tu_berlin.mpds.metric_collector.model.flinkmetric.JobVertex;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.PrometheusJsonResponse;
import de.tu_berlin.mpds.metric_collector.util.FlinkQuery;
import de.tu_berlin.mpds.metric_collector.configuration.ApplicationConfiguration;
import de.tu_berlin.mpds.metric_collector.util.PrometheusQuery;
import java.util.List;
import javax.annotation.PostConstruct;
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
  @Autowired
  private ApplicationConfiguration applicationConfiguration;




  //@PostConstruct
  @Scheduled(cron = "0/2 * * * * ?")
  public void init() throws InterruptedException, ExecutionException, IOException {
    //we are printing the response for now!!!

    PrometheusJsonResponse responseFlinkTaskManagerJVMCPULoad = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD(),client,objectMapper);

    System.out.println("responseFlinkTaskManagerJVMCPULoad " + responseFlinkTaskManagerJVMCPULoad.getData().getResult().get(0).getValue().get(1));

    PrometheusJsonResponse  responseFlinkJobManagerJVMCPULoad = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_JOBMANAGER_STATUS_JVM_CPU_LOAD(),client,objectMapper);
    System.out.println("responseFlinkJobManagerJVMCPULoad " + responseFlinkJobManagerJVMCPULoad.getData().getResult().get(0).getValue().get(1));

    PrometheusJsonResponse  responseFlinkNumOfTaskManager = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS(),client,objectMapper);
    System.out.println("responseFlinkNumOfTaskManager " + responseFlinkNumOfTaskManager.getData().getResult().get(0).getValue().get(1));

    PrometheusJsonResponse  responseFlinkNumOfRunningJobs = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS(),client,objectMapper);
    System.out.println("responseFlinkNumOfRunningJobs " + responseFlinkNumOfRunningJobs.getData().getResult().get(0).getValue().get(1));

    PrometheusJsonResponse  responseFlinkJVMMemoryTaskManagerRatio = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO(),client,objectMapper);
    System.out.println("responseFlinkJVMMemoryTaskManagerRatio " + responseFlinkJVMMemoryTaskManagerRatio.getData().getResult().get(0).getValue().get(1));

    PrometheusJsonResponse responseFlinkJVMMemoryJobManagerRatio = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_JVM_MEMORY_JOBMANAGER_RATIO(),client,objectMapper);
    System.out.println("responseFlinkJVMMemoryJobManagerRatio " + responseFlinkJVMMemoryJobManagerRatio.getData().getResult().get(0).getValue().get(1));


    List<Job> jobs = flinkAPIMetricService.getJobs(flinkQuery.getFLINK_JOBS_OVERVIEW(),client,objectMapper);
    System.out.println("Received " + jobs.size() + " job(s):");

    for (Job job : jobs) {
      System.out.println("Job " + job.getJid() + ": " + job.getName() + " (" + job.getState()+ ")");
      Job job_info = flinkAPIMetricService.getJobInfo(flinkQuery.getFLINK_JOBS() + job.getJid(),client,objectMapper);
      System.out.println("The job has the following vertices: ");
      for (JobVertex vertex : job_info.getVertices()) {
        System.out.println("Name: " + vertex.getName());
        System.out.println("Parallelism: " + vertex.getParallelism());
        System.out.println("Status: " + vertex.getStatus());
        System.out.println("---");
      }
      System.out.println("------");

    }


  }
}
