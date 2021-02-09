package de.tu_berlin.mpds.metric_collector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.model.flinkmetric.Job;
import de.tu_berlin.mpds.metric_collector.model.flinkmetric.JobVertex;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.PrometheusJsonResponse;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.Result;
import de.tu_berlin.mpds.metric_collector.util.FlinkQuery;
import de.tu_berlin.mpds.metric_collector.configuration.ApplicationConfiguration;
import de.tu_berlin.mpds.metric_collector.util.PrometheusQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;

import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutionException;

@Service
public class RequestMetricService {


    private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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


    public void init() throws InterruptedException, ExecutionException, JsonProcessingException {
        /*
      PROCESS:

      get all jobs and their configuration (task_subtasks)
      initialize {task_subtask: capacity} mapping
      while True:

        for every job (running or stopped):
             - get the tasks and their subtasks (parallelism)
             - find timestamps where task_subtask backpressure > 0.5
             - for those task_subtasks:
                  - find number of processed messages during that time:
                    - globally (number of incoming kafka messages) -> capacity of the job
                    - outgoing messages per task_subtask  -> capacity of one node
                  - update {task_subtask: capacity}
        if {task_subtask: capacity} has not changed:
          break

        else:
          increase load / inject failures


         */

        System.out.println("Initializing the system.");
        System.out.println("Looking for Jobs (running and stopped)...");
        List<Job> jobs = flinkAPIMetricService.getJobs(client, objectMapper);
        System.out.println("Found " + jobs.size() + " job(s):");
        for (Job job : jobs) {
            System.out.println("Analyzing data for job " + job.getJid() + ": "
                    + job.getName() + " (" + job.getState() + ")");
            Job job_info = flinkAPIMetricService.getJobInfo(job.getJid(), client, objectMapper);

            System.out.println("The job has the following Tasks: ");
            for (JobVertex task : job_info.getVertices()) {
                System.out.println("Name: " + task.getName());
                System.out.println("Parallelism: " + task.getParallelism());
                System.out.println("Status: " + task.getStatus());
            }

        }
        ZonedDateTime endDate = ZonedDateTime.now().plusDays(1).with(LocalTime.of(0, 0));
        // go back 7 days
        ZonedDateTime startDate = endDate.minusDays(7);

        Map<String, List<Map<Integer, Double>>> max_values = new HashMap<>();

        // get all the timestamps where backpressure > 0.5
        while (!startDate.isAfter(endDate)) {

        }

        // calculate the max and the corresponding timestamp

        // get config at that time

        // get cpu and mem load at that time

        // print
    }

    //@PostConstruct
    @Scheduled(cron = "0/2 * * * * ?")
    public void run() throws InterruptedException, ExecutionException, IOException {
        this.init();
        System.out.println("---- PROMETHEUS QUERIES -----");

        //we are printing the response for now!!!
        PrometheusJsonResponse responseFlinkTaskManagerJVMCPULoad = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD(), client, objectMapper);
        List<Result> taskCPUQueryResults = responseFlinkTaskManagerJVMCPULoad.getData().getResult();
        PrometheusJsonResponse responseFlinkJVMMemoryTaskManagerRatio = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO(), client, objectMapper);
        List<Result> taskMemoryQueryResults = responseFlinkJVMMemoryTaskManagerRatio.getData().getResult();

        System.out.println("Found " + taskCPUQueryResults.size() + " Task Manager(s)");
        for (int i = 0; i < taskCPUQueryResults.size(); i++) {
            System.out.println("Pod name: " + taskCPUQueryResults.get(i).getMetric().getKubernetesPodName());
            System.out.println("Memory load: " + taskMemoryQueryResults.get(i).getValue().get(1));
            System.out.println("CPU load: " + taskCPUQueryResults.get(i).getValue().get(1));
            System.out.println("----");
        }

        PrometheusJsonResponse responseFlinkJobManagerJVMCPULoad = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_JOBMANAGER_STATUS_JVM_CPU_LOAD(), client, objectMapper);
        List<Result> jobCPUQueryResults = responseFlinkJobManagerJVMCPULoad.getData().getResult();
        PrometheusJsonResponse responseFlinkJVMMemoryJobManagerRatio = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_JVM_MEMORY_JOBMANAGER_RATIO(), client, objectMapper);
        List<Result> jobMemoryQueryResults = responseFlinkJVMMemoryJobManagerRatio.getData().getResult();
        System.out.println("----");
        System.out.println("Found " + jobCPUQueryResults.size() + " Job Manager(s)");
        for (int i = 0; i < jobCPUQueryResults.size(); i++) {
            System.out.println("Pod name: " + jobCPUQueryResults.get(i).getMetric().getKubernetesPodName());
            System.out.println("Memory load: " + jobMemoryQueryResults.get(i).getValue().get(1));
            System.out.println("CPU load: " + jobCPUQueryResults.get(i).getValue().get(1));
            System.out.println("----");
        }

        PrometheusJsonResponse responseFlinkNumOfRunningJobs = prometheusMetricService.sendRequestToPrometheusForMetric(prometheusQuery.getQUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS(), client, objectMapper);
        System.out.println("Number of running jobs: " + responseFlinkNumOfRunningJobs.getData().getResult().get(0).getValue().get(1));


        System.out.println("---- FLINK API -----");
        List<Job> jobs = flinkAPIMetricService.getJobs(client, objectMapper);
        System.out.println("Received " + jobs.size() + " job(s):");


        for (Job job : jobs) {
            System.out.println("Job " + job.getJid() + ": " + job.getName() + " (" + job.getState() + ")");
            Job job_info = flinkAPIMetricService.getJobInfo(job.getJid(), client, objectMapper);
            System.out.println("The job has the following vertices: ");
            Integer prev_records_out = -1;
            for (JobVertex vertex : job_info.getVertices()) {
                System.out.println("Name: " + vertex.getName());
                System.out.println("Parallelism: " + vertex.getParallelism());
                System.out.println("Status: " + vertex.getStatus());
                System.out.println("Number of incoming records: " + vertex.getMetrics().getRead_records());
                System.out.println("Number of outgoing records: " + vertex.getMetrics().getWrite_records());
                if (prev_records_out >= 0) {
                    int lag = prev_records_out - vertex.getMetrics().getRead_records();
                    System.out.println("->> Vertex is lagging behind by " + lag + " records");
                }
                prev_records_out = vertex.getMetrics().getWrite_records();
                System.out.println("---");
            }
            System.out.println("------");

        }


    }
}
