package de.tu_berlin.mpds.metric_collector.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.configuration.PrometheusConfig;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.PrometheusJsonResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class RequestPrometheusMetricService {

  private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
  private final ObjectMapper objectMapper = new ObjectMapper();

  //prepare urls with query
  String queryFlinkTaskManagerJVMCPULoad = PrometheusConfig.BASE_URL_PROMETHEUS + PrometheusConfig.QUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD;
  String queryFlinkJobManagerJVMCPULoad = PrometheusConfig.BASE_URL_PROMETHEUS + PrometheusConfig.QUERY_FLINK_JOBMANAGER_STATUS_JVM_CPU_LOAD;
  String queryFlinkNumOfTaskManager = PrometheusConfig.BASE_URL_PROMETHEUS + PrometheusConfig.QUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS;
  String queryFlinkNumOfRunningJobs = PrometheusConfig.BASE_URL_PROMETHEUS + PrometheusConfig.QUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS;
  String queryFlinkJVMMemoryTaskManagerRatio = PrometheusConfig.BASE_URL_PROMETHEUS + PrometheusConfig.QUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO;
  String queryFlinkJVMMemoryJobManagerRatio = PrometheusConfig.BASE_URL_PROMETHEUS + PrometheusConfig.QUERY_FLINK_JVM_MEMORY_JOBMANAGER_RATIO;


  //this method is used for printing the output of the request after the application runs
  //logic may change or new runner class will introduce later.
  //@PostConstruct
  @Scheduled(cron = "0/2 * * * * ?")
  public void init() throws InterruptedException, ExecutionException, IOException {
    //we are printing the response for now!!!
    PrometheusJsonResponse  responseFlinkTaskManagerJVMCPULoad = sendRequestToPrometheusForMetric(queryFlinkTaskManagerJVMCPULoad);
    System.out.println("responseFlinkTaskManagerJVMCPULoad " + responseFlinkTaskManagerJVMCPULoad.getData().getResult().get(0).getValue().get(1));

    PrometheusJsonResponse  responseFlinkJobManagerJVMCPULoad = sendRequestToPrometheusForMetric(queryFlinkJobManagerJVMCPULoad);
    System.out.println("responseFlinkJobManagerJVMCPULoad " + responseFlinkJobManagerJVMCPULoad.getData().getResult().get(0).getValue().get(1));

    PrometheusJsonResponse  responseFlinkNumOfTaskManager = sendRequestToPrometheusForMetric(queryFlinkNumOfTaskManager);
    System.out.println("responseFlinkNumOfTaskManager " + responseFlinkNumOfTaskManager.getData().getResult().get(0).getValue().get(1));

    PrometheusJsonResponse  responseFlinkNumOfRunningJobs = sendRequestToPrometheusForMetric(queryFlinkNumOfRunningJobs);
    System.out.println("responseFlinkNumOfRunningJobs " + responseFlinkNumOfRunningJobs.getData().getResult().get(0).getValue().get(1));

    PrometheusJsonResponse  responseFlinkJVMMemoryTaskManagerRatio = sendRequestToPrometheusForMetric(queryFlinkJVMMemoryTaskManagerRatio);
    System.out.println("responseFlinkJVMMemoryTaskManagerRatio " + responseFlinkJVMMemoryTaskManagerRatio.getData().getResult().get(0).getValue().get(1));

    PrometheusJsonResponse  responseFlinkJVMMemoryJobManagerRatio = sendRequestToPrometheusForMetric(queryFlinkJVMMemoryJobManagerRatio);
    System.out.println("responseFlinkJVMMemoryJobManagerRatio " + responseFlinkJVMMemoryJobManagerRatio.getData().getResult().get(0).getValue().get(1));
  }


  private PrometheusJsonResponse sendRequestToPrometheusForMetric(String urlWithQuery) throws
          InterruptedException, ExecutionException, IOException {

    HttpRequest request = HttpRequest.newBuilder(URI.create(urlWithQuery)).GET().build();

    CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

    return objectMapper.readValue(response.get().body(),PrometheusJsonResponse.class);
  }

}
