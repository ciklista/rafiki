package de.tu_berlin.mpds.metric_collector.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.model.db.KafkaMetrics;
import de.tu_berlin.mpds.metric_collector.model.db.OperatorMetrics;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.Job;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.JobSubtask;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.JobVertex;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.JobsResponse;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.PrometheusJsonResponse;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.Result;
import de.tu_berlin.mpds.metric_collector.util.FlinkQuery;
import de.tu_berlin.mpds.metric_collector.util.PrometheusQuery;

import java.math.BigInteger;
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


  // @PostConstruct
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

   */
   }

    @Scheduled(cron = "0/2 * * * * ?")
    public void live_run() throws InterruptedException, ExecutionException, IOException {

        System.out.println("---- PROMETHEUS QUERIES -----");
       KafkaMetrics kafkaMetrics= gatherKafkaMetric(client,objectMapper);
      System.out.println(kafkaMetrics.toString());

    }
    private void printQueryResults(List<Result> jobCPUQueryResults, List<Result> jobMemoryQueryResults) {
        for (int i = 0; i < jobCPUQueryResults.size(); i++) {
            System.out.println("Pod name: " + jobCPUQueryResults.get(i).getMetric().getKubernetesPodName());
            System.out.println("Memory load: " + jobMemoryQueryResults.get(i).getValue().get(1));
            System.out.println("CPU load: " + jobCPUQueryResults.get(i).getValue().get(1));
            System.out.println("----");
        }
    }

  protected KafkaMetrics gatherKafkaMetric(HttpClient client, ObjectMapper objectMapper) throws InterruptedException, ExecutionException, IOException {
    String operatorId;
    KafkaMetrics kafkaMetrics = new KafkaMetrics();
    PrometheusJsonResponse FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX = null;
    PrometheusJsonResponse KAFKA_MESSAGE_IN_PER_SEC = null;
    List<Job> jobsResponse = flinkAPIService.getJobs(client, objectMapper);
    for(Job job: jobsResponse){
      if(job.getState().equals("RUNNING")) {
        Job jobInfo = flinkAPIService.getJobInfo(job.getJid(), client, objectMapper);
        for(int i=0; i < jobInfo.getVertices().size(); i++){
          operatorId = jobInfo.getVertices().get(i).getId();
           FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX =  prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX(), client, objectMapper);
           KAFKA_MESSAGE_IN_PER_SEC = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_KAFKA_MESSAGE_IN_PER_SEC(operatorId), client, objectMapper);
          long kafkaLag = FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX.getData().getResult().get(1).getValue().get(1);
          long kafkaMessagesInPerSecond = KAFKA_MESSAGE_IN_PER_SEC.getData().getResult().get(1).getValue().get(1);
          kafkaMetrics.setMaxKafkaLag(kafkaLag);
          kafkaMetrics.setMaxKafkaMessagesPerSecond(kafkaMessagesInPerSecond);
        }
      }
    }


    return kafkaMetrics;
  }

  protected OperatorMetrics gatherOperatorMetric( HttpClient client, ObjectMapper objectMapper) throws InterruptedException, ExecutionException, IOException {
    String operatorId;
    String operatorCompact = "";
    String operatorName;
    int parallelism = 0;
    long bytesIn = 0;
    long recordsIn = 0;
    long bytesOut = 0;
    long recordsOut = 0;
    long backPressure = 0;
    long latency = 0;
    OperatorMetrics operatorMetrics = new OperatorMetrics();

     List<Job> jobsResponse = flinkAPIService.getJobs(client, objectMapper);
    for(Job job: jobsResponse){
      if(job.getState().equals("RUNNING")){
        Job jobInfo = flinkAPIService.getJobInfo(job.getJid(), client, objectMapper);

        for(int i=0; i < jobInfo.getVertices().size(); i++){
          operatorId = jobInfo.getVertices().get(i).getId();
          operatorName= jobInfo.getVertices().get(i).getName();
          operatorCompact = operatorId+operatorName;
          parallelism= jobInfo.getVertices().get(i).getParallelism();
          bytesIn = jobInfo.getVertices().get(i).getSubtasks().get(1).getMetrics().getRead_bytes();
          recordsIn = jobInfo.getVertices().get(i).getSubtasks().get(1).getMetrics().getRead_records();
          bytesOut= jobInfo.getVertices().get(i).getSubtasks().get(1).getMetrics().getWrite_bytes();
          recordsOut= jobInfo.getVertices().get(i).getSubtasks().get(1).getMetrics().getWrite_records();
          PrometheusJsonResponse prometheusJsonResponse = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_FLINK_TASKMANAGER_IS_BACK_PRESSURE(operatorId),client,objectMapper);
          PrometheusJsonResponse prometheusJsonResponse1 = prometheusMetricService.executePrometheusQuery(prometheusQuery.getflink_taskmanager_job_latency(operatorId),client,objectMapper);
          backPressure = prometheusJsonResponse.getData().getResult().get(1).getValue().get(1);
          latency = prometheusJsonResponse1.getData().getResult().get(1).getValue().get(1);
        }
        operatorMetrics.setMaxBackPresure(backPressure);
        operatorMetrics.setMaxLatency(latency);
        operatorMetrics.setOperatorParallelism(parallelism);
        operatorMetrics.setOperatorId(operatorCompact);
        operatorMetrics.setMaxBytesIn(bytesIn);
        operatorMetrics.setMaxBytesOut(bytesOut);
        operatorMetrics.setMaxRecordsIn(recordsIn);
        operatorMetrics.setMaxRecordsOut(recordsOut);
      }
    }
    return operatorMetrics;
  }

}
