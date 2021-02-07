package de.tu_berlin.mpds.metric_collector.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.configuration.FlinkConfig;
import de.tu_berlin.mpds.metric_collector.configuration.PrometheusConfig;
import de.tu_berlin.mpds.metric_collector.model.flinkmetric.Job;
import de.tu_berlin.mpds.metric_collector.model.flinkmetric.JobVertex;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.PrometheusJsonResponse;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class RequestMetricService {

    private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private FlinkAPIMetricService flinkAPIMetricService;
    @Autowired
    private PrometheusMetricService prometheusMetricService;

    //prepare urls with query for prometheus
    String queryFlinkTaskManagerJVMCPULoad = PrometheusConfig.BASE_URL_PROMETHEUS + PrometheusConfig.QUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD;
    String queryFlinkJobManagerJVMCPULoad = PrometheusConfig.BASE_URL_PROMETHEUS + PrometheusConfig.QUERY_FLINK_JOBMANAGER_STATUS_JVM_CPU_LOAD;
    String queryFlinkNumOfTaskManager = PrometheusConfig.BASE_URL_PROMETHEUS + PrometheusConfig.QUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS;
    String queryFlinkNumOfRunningJobs = PrometheusConfig.BASE_URL_PROMETHEUS + PrometheusConfig.QUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS;
    String queryFlinkJVMMemoryTaskManagerRatio = PrometheusConfig.BASE_URL_PROMETHEUS + PrometheusConfig.QUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO;
    String queryFlinkJVMMemoryJobManagerRatio = PrometheusConfig.BASE_URL_PROMETHEUS + PrometheusConfig.QUERY_FLINK_JVM_MEMORY_JOBMANAGER_RATIO;

    //prepare urls with query for flink
    String queryJobs = FlinkConfig.BASE_URL_FLINK + FlinkConfig.FLINK_JOBS_OVERVIEW;
    String queryJob = FlinkConfig.BASE_URL_FLINK + FlinkConfig.FLINK_JOBS;


    //this method is used for printing the output of the request after the application runs
    //logic may change or new runner class will introduce later.
    //@PostConstruct
    @Scheduled(cron = "0/2 * * * * ?")
    public void init() throws InterruptedException, ExecutionException, IOException {
        System.out.println("---- PROMETHEUS QUERIES -----");

        //we are printing the response for now!!!
        PrometheusJsonResponse responseFlinkTaskManagerJVMCPULoad = prometheusMetricService.sendRequestToPrometheusForMetric(queryFlinkTaskManagerJVMCPULoad, client, objectMapper);
        List<Result> taskCPUQueryResults = responseFlinkTaskManagerJVMCPULoad.getData().getResult();
        PrometheusJsonResponse responseFlinkJVMMemoryTaskManagerRatio = prometheusMetricService.sendRequestToPrometheusForMetric(queryFlinkJVMMemoryTaskManagerRatio, client, objectMapper);
        List<Result> taskMemoryQueryResults = responseFlinkJVMMemoryTaskManagerRatio.getData().getResult();

        System.out.println("Found " + taskCPUQueryResults.size() + " Task Manager(s)");
        for (int i = 0; i < taskCPUQueryResults.size(); i++) {
            System.out.println("Pod name: " + taskCPUQueryResults.get(i).getMetric().getKubernetesPodName());
            System.out.println("Memory load: " + taskMemoryQueryResults.get(i).getValue().get(1));
            System.out.println("CPU load: " + taskCPUQueryResults.get(i).getValue().get(1));
            System.out.println("----");
        }

        PrometheusJsonResponse responseFlinkJobManagerJVMCPULoad = prometheusMetricService.sendRequestToPrometheusForMetric(queryFlinkJobManagerJVMCPULoad, client, objectMapper);
        List<Result> jobCPUQueryResults = responseFlinkJobManagerJVMCPULoad.getData().getResult();
        PrometheusJsonResponse responseFlinkJVMMemoryJobManagerRatio = prometheusMetricService.sendRequestToPrometheusForMetric(queryFlinkJVMMemoryJobManagerRatio, client, objectMapper);
        List<Result> jobMemoryQueryResults = responseFlinkJVMMemoryJobManagerRatio.getData().getResult();
        System.out.println("----");
        System.out.println("Found " + jobCPUQueryResults.size() + " Job Manager(s)");
        for (int i = 0; i < jobCPUQueryResults.size(); i++) {
            System.out.println("Pod name: " + jobCPUQueryResults.get(i).getMetric().getKubernetesPodName());
            System.out.println("Memory load: " + jobMemoryQueryResults.get(i).getValue().get(1));
            System.out.println("CPU load: " + jobCPUQueryResults.get(i).getValue().get(1));
            System.out.println("----");
        }

        PrometheusJsonResponse responseFlinkNumOfRunningJobs = prometheusMetricService.sendRequestToPrometheusForMetric(queryFlinkNumOfRunningJobs, client, objectMapper);
        System.out.println("Number of running jobs: " + responseFlinkNumOfRunningJobs.getData().getResult().get(0).getValue().get(1));


        System.out.println("---- FLINK API -----");
        List<Job> jobs = flinkAPIMetricService.getJobs(queryJobs, client, objectMapper);
        System.out.println("Received " + jobs.size() + " job(s):");

        for (Job job : jobs) {
            System.out.println("Job " + job.getJid() + ": " + job.getName() + " (" + job.getState() + ")");
            Job job_info = flinkAPIMetricService.getJobInfo(queryJob + job.getJid(), client, objectMapper);
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
