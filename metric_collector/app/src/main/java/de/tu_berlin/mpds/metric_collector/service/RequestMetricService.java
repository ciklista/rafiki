package de.tu_berlin.mpds.metric_collector.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.Job;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.JobSubtask;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.JobVertex;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.PrometheusJsonResponse;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.Result;
import de.tu_berlin.mpds.metric_collector.util.FlinkQuery;
import de.tu_berlin.mpds.metric_collector.util.PrometheusQuery;

import java.sql.Timestamp;
import java.util.List;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutionException;

@Service
public class RequestMetricService {


    private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ;
    @Autowired
    private FlinkAPIService flinkAPIService;
    @Autowired
    private PrometheusMetricService prometheusMetricService;
    @Autowired
    private PrometheusQuery prometheusQuery;
    @Autowired
    private FlinkQuery flinkQuery;


   @PostConstruct
    public void init() throws InterruptedException, ExecutionException, IOException {
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

        // STEP 1
        System.out.println("Initializing the system.");
        System.out.println("Looking for Jobs (running and stopped)...");
        List<Job> jobs = flinkAPIService.getJobs(client, objectMapper);
        System.out.println("Found " + jobs.size() + " job(s):");
        for (Job job : jobs) {
            System.out.println("Analyzing data for job " + job.getJid() + ": "
                    + job.getName() + " (" + job.getState() + ")");
            Job job_info = flinkAPIService.getJobInfo(job.getJid(), client, objectMapper);
            job.setVertices(job_info.getVertices());
            System.out.println("The job has the following Tasks: ");
            for (JobVertex task : job_info.getVertices()) {
                System.out.println("\t- Name: " + task.getName());
                System.out.println("\t  Parallelism: " + task.getParallelism());
            }
            System.out.println("-----------");
        }

        // STEP 2
        System.out.println("Looking for back pressure timestamps...");
        // query prometheus data
        System.out.println("Query Prometheus...");
        Map<String, List<List<Double>>> dbData = prometheusMetricService.getBackpressuredSubTasks(7);
        System.out.println("Searching detected jobs in Prometheus query results");
        for (Job job : jobs) {
            for (JobVertex task : job.getVertices()) {
                for (JobSubtask subtask : task.getSubtasks()) {
                    String search_key = job.getJid() + "_" + task.getId() + "_" + subtask.getId();
                    if (dbData.containsKey(search_key)) {
                        System.out.println("Found data for Job: " + job.getJid() + ", Task: " + task.getId()
                                + ", Subtask: " + subtask.getId());
                        for (List<Double> metric : dbData.get(search_key)) {
                            Timestamp ts = new Timestamp(metric.get(0).longValue() * 1000);
                            Date date = new Date(ts.getTime());
                            System.out.println("\tValue: " + metric.get(1) + " @ " + date);
                        }
                        ;
                    }
                }
            }

        }
     System.out.println("----------");
     System.out.println("Finished initialization");
     System.out.println("----------");


   }

    @Scheduled(cron = "0/2 * * * * ?")
    public void live_run() throws InterruptedException, ExecutionException, IOException {
        System.out.println("---- PROMETHEUS QUERIES -----");
        try {
            PrometheusJsonResponse responseFlinkTaskManagerJVMCPULoad = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_FLINK_TASKMANAGER_STATUS_JVM_CPU_LOAD(), client, objectMapper);
            List<Result> taskCPUQueryResults = responseFlinkTaskManagerJVMCPULoad.getData().getResult();
            PrometheusJsonResponse responseFlinkJVMMemoryTaskManagerRatio = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_FLINK_JVM_MEMORY_TASKMANAGER_RATIO(), client, objectMapper);
            List<Result> taskMemoryQueryResults = responseFlinkJVMMemoryTaskManagerRatio.getData().getResult();

            System.out.println("Found " + taskCPUQueryResults.size() + " Task Manager(s)");
            printQueryResults(taskCPUQueryResults, taskMemoryQueryResults);

            PrometheusJsonResponse responseFlinkJobManagerJVMCPULoad = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_FLINK_JOBMANAGER_STATUS_JVM_CPU_LOAD(), client, objectMapper);
            List<Result> jobCPUQueryResults = responseFlinkJobManagerJVMCPULoad.getData().getResult();
            PrometheusJsonResponse responseFlinkJVMMemoryJobManagerRatio = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_FLINK_JVM_MEMORY_JOBMANAGER_RATIO(), client, objectMapper);
            List<Result> jobMemoryQueryResults = responseFlinkJVMMemoryJobManagerRatio.getData().getResult();
            System.out.println("----");
            System.out.println("Found " + jobCPUQueryResults.size() + " Job Manager(s)");
            printQueryResults(jobCPUQueryResults, jobMemoryQueryResults);

            PrometheusJsonResponse responseFlinkNumOfRunningJobs = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_FLINK_JOBMANAGER_NUM_RUNNING_JOBS(), client, objectMapper);
            System.out.println("Number of running jobs: " + responseFlinkNumOfRunningJobs.getData().getResult().get(0).getValue().get(1));

            PrometheusJsonResponse responseFlinkNumOfRunningTaskManager = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_FLINK_JOBMANAGER_NUM_REGISTERED_TASK_MANAGERS(), client, objectMapper);
            System.out.println("Number of running TaskManagers: " + responseFlinkNumOfRunningTaskManager.getData().getResult().get(0).getValue().get(1));

            PrometheusJsonResponse responseQUERY_KAFKA_MESSAGE_IN_PER_SEC = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_KAFKA_MESSAGE_IN_PER_SEC(), client, objectMapper);
            System.out.println("QUERY_KAFKA_MESSAGE_IN_PER_SEC: " + responseQUERY_KAFKA_MESSAGE_IN_PER_SEC.getData().getResult().get(0).getValue().get(1));

            PrometheusJsonResponse responseQUERY_KAFKA_BYTES_IN_PER_SEC = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_KAFKA_BYTES_IN_PER_SEC(), client, objectMapper);
            System.out.println("QUERY_KAFKA_BYTES_IN_PER_SEC: " + responseQUERY_KAFKA_BYTES_IN_PER_SEC.getData().getResult().get(0).getValue().get(1));

            PrometheusJsonResponse responseQUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN(), client, objectMapper);
            System.out.println("QUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN: " + responseQUERY_FLINK_TASKMANAGER_NUM_RECORDS_IN.getData().getResult().get(0).getValue().get(1));

            PrometheusJsonResponse responseQUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT(), client, objectMapper);
            System.out.println("QUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT: " + responseQUERY_FLINK_TASKMANAGER_OPERATOR_NUM_RECORDS_OUT.getData().getResult().get(0).getValue().get(1));

            PrometheusJsonResponse responseQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_OPERATOR_ID = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_OPERATOR_ID(), client, objectMapper);
            System.out.println("QUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_OPERATOR_ID: " + responseQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_OPERATOR_ID.getData().getResult().get(0).getValue().get(1));

            PrometheusJsonResponse responseQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_QUANTILE = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_QUANTILE(), client, objectMapper);
            System.out.println("QUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_QUANTILE: " + responseQUERY_FLINK_TASKMANAGER_SUBTASK_LATENCY_AVG_BY_QUANTILE.getData().getResult().get(0).getValue().get(1));

            PrometheusJsonResponse responseQUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE(), client, objectMapper);
            System.out.println("QUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE: " + responseQUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE.getData().getResult().get(0).getValue().get(1));

            PrometheusJsonResponse responseQUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX(), client, objectMapper);
            System.out.println("QUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX: " + responseQUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX.getData().getResult().get(0).getValue().get(1));


            System.out.println("---- FLINK API -----");
            List<Job> jobs = flinkAPIService.getJobs(client, objectMapper);
            System.out.println("Received " + jobs.size() + " job(s):");

            for (Job job : jobs) {
                System.out.println("Job " + job.getJid() + ": " + job.getName() + " (" + job.getState() + ")");
                Job job_info = flinkAPIService.getJobInfo(job.getJid(), client, objectMapper);
                System.out.println("The job has the following vertices: ");
                for (JobVertex vertex : job_info.getVertices()) {
                    System.out.println("Name: " + vertex.getName());
                    System.out.println("Parallelism: " + vertex.getParallelism());
                    System.out.println("Status: " + vertex.getStatus());
                    System.out.println("---");
                }
                System.out.println("------");

            }

        } catch (NullPointerException e) {
            System.out.println("Null pointer exception");
        } catch (IndexOutOfBoundsException e) {
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
