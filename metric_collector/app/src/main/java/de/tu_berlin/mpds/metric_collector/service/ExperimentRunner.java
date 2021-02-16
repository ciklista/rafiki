package de.tu_berlin.mpds.metric_collector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.Job;
import de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.Operator;
import de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.OperatorMetric;
import de.tu_berlin.mpds.metric_collector.model.experiments.RunConfiguration;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.JarRunResponse;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.JobVertex;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.Result;
import de.tu_berlin.mpds.metric_collector.util.FlinkQuery;
import de.tu_berlin.mpds.metric_collector.util.ParallelismExperimentsPlanner;
import de.tu_berlin.mpds.metric_collector.util.PrometheusQuery;

import java.sql.SQLException;
import java.util.List;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class ExperimentRunner {


    private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private FlinkAPIService flinkAPIService;
    @Autowired
    private PrometheusMetricService prometheusMetricService;
    @Autowired
    private PrometheusQuery prometheusQuery;
    @Autowired
    private FlinkQuery flinkQuery;
    @Autowired
    private ParallelismExperimentsPlanner experimentPlanner;
    @Autowired
    private DatabaseService databaseService;

    @Bean
    public void entrypoint() throws InterruptedException, ExecutionException, IOException, SQLException {
        // get max parallelism
        String[] operatorNames = new String[]{"deserializebolt", "EventFilterBolt", "project", "RedisJoinBolt", "CampaignProcessor"};

        List<String> configs = experimentPlanner.getJobArgs(client, objectMapper, operatorNames, 1, "1000");

        for (String jobArg : configs) {
            // hard coded for now, will be passed through GUI
            run_experiment("29b1cf4b-7322-4e89-9a55-6b8ab0227caa_processor-1.0-SNAPSHOT.jar", jobArg, 5);
        }
    }

    public void run_experiment(String jarID, String programArgs, int experimentDuration) throws InterruptedException, ExecutionException, IOException, SQLException {
        // start job with given config
        JarRunResponse response = flinkAPIService.runJar(client, objectMapper, jarID, programArgs, "1");
        String jobId = response.getJobID();
        System.out.println("started job: " + jobId);
        // generate experiment_id
        String experimentId = UUID.randomUUID().toString();

        de.tu_berlin.mpds.metric_collector.model.flinkapi.Job jobInfo = flinkAPIService.getJobInfo(jobId, client, objectMapper);
        List<JobVertex> jobVertices = jobInfo.getVertices();

        // run experiment + collect metrics
        long experimentStarted = System.currentTimeMillis() / 1000L;

        HashMap<String, OperatorMetric> maxOperatorMetrics = gatherMetrics(experimentDuration, jobId, experimentId, jobVertices);

        long experimentStopped = System.currentTimeMillis() / 1000L;
        System.out.println("Experiment done.");
        flinkAPIService.cancelJob(client, objectMapper, jobId);
        System.out.println("Cancelled job.");


        // update db
        Job job = new Job(jobId, jobInfo.getName());

        List<Operator> jobOperators = new ArrayList<>();
        for (JobVertex vertex : jobVertices) {
            jobOperators.add(new Operator(vertex.getId(), jobId, vertex.getName(), "", ""));
        }

        de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.Result experimentResult = new de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.Result(experimentId, jobId, experimentStarted, experimentStopped);

        List<OperatorMetric> operatorMetricList = new ArrayList<>(maxOperatorMetrics.values());
        System.out.println("Sending results to database...");
        databaseService.insertJobs(job);
        databaseService.insertOperators(jobOperators);
        databaseService.insertResults(experimentResult);
        databaseService.insertOperatorMetrics(operatorMetricList);
        System.out.println("...done.");

    }

    public HashMap<String, OperatorMetric> gatherMetrics(int durationSec, String jobId, String experimentId, List<JobVertex> vertices) throws InterruptedException, ExecutionException, IOException {
        HashMap<String, OperatorMetric> maxOperatorMetrics = new HashMap<>();
        StringBuilder parallelismPrintString = new StringBuilder();
        for (JobVertex vertex : vertices) {
            maxOperatorMetrics.put(vertex.getId(), new OperatorMetric(experimentId, vertex.getId(), jobId, vertex.getParallelism(), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
            parallelismPrintString.append("\t").append(vertex.getName()).append(" - Parallelism: ").append(vertex.getParallelism()).append("\n");
        }
        System.out.println("Starting experiment for job " + jobId +  " with the following parallelism config:");
        System.out.println(parallelismPrintString);
        for (long stop = System.nanoTime() + TimeUnit.SECONDS.toNanos(durationSec); stop > System.nanoTime(); ) {
            updateOperatorMetricsForJob(client, objectMapper, jobId, maxOperatorMetrics);
        }
        System.out.println("Finished experiment for job: " + jobId);
        return maxOperatorMetrics;
    }


//    protected KafkaMetric gatherKafkaMetric(HttpClient client, ObjectMapper objectMapper) throws InterruptedException, ExecutionException, IOException {
//        String operatorId;
//        KafkaMetric kafkaMetric = new KafkaMetric();
//        PrometheusJsonResponse FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX = null;
//        PrometheusJsonResponse KAFKA_MESSAGE_IN_PER_SEC = null;
//        List<Job> jobsResponse = flinkAPIService.getJobs(client, objectMapper);
//        for (Job job : jobsResponse) {
//            if (job.getState().equals("RUNNING")) {
//                Job jobInfo = flinkAPIService.getJobInfo(job.getJid(), client, objectMapper);
//                for (int i = 0; i < jobInfo.getVertices().size(); i++) {
//                    operatorId = jobInfo.getVertices().get(i).getId();
//                    FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX(), client, objectMapper);
//                    KAFKA_MESSAGE_IN_PER_SEC = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_KAFKA_MESSAGE_IN_PER_SEC(operatorId), client, objectMapper);
//                    double kafkaLag = FLINK_TASKMANAGER_KAFKACONSUMER_RECORD_LAG_MAX.getData().getResult().get(1).getValue().get(1);
//                    double kafkaMessagesInPerSecond = KAFKA_MESSAGE_IN_PER_SEC.getData().getResult().get(1).getValue().get(1);
//                    kafkaMetric.setKafkaLag(kafkaLag);
//                    kafkaMetric.setKafkaMessagesPerSecond(kafkaMessagesInPerSecond);
//                }
//            }
//        }
//
//
//        return kafkaMetric;
//    }

    private void updateOperatorMetricsForJob(HttpClient client, ObjectMapper objectMapper, String jobId, HashMap<String, OperatorMetric> operatorMetrics)
            throws InterruptedException, ExecutionException, IOException {

        List<Result> bytesIn = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_BYTES_IN_BY_TASK(jobId), client, objectMapper).getData().getResult();
        List<Result> bytesOut = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_BYTES_OUT_BY_TASK(jobId), client, objectMapper).getData().getResult();
        List<Result> messagesIn = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_MESSAGES_IN_BY_TASK(jobId), client, objectMapper).getData().getResult();
        List<Result> messagesOut = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_MESSAGES_OUT_BY_TASK(jobId), client, objectMapper).getData().getResult();
        List<Result> backpressure = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_MAX_BACKPRESSURE_BY_TASK(jobId), client, objectMapper).getData().getResult();
        List<Result> latency = prometheusMetricService.executePrometheusQuery(prometheusQuery.getQUERY_AVG_LATENCY_BY_TASK(jobId), client, objectMapper).getData().getResult();

        for (Result result : bytesIn) {
            OperatorMetric current_value = operatorMetrics.get(result.getMetric().getTaskId());
            if (current_value.getBytesIn() < result.getValue().get(1)) {
                current_value.setBytesIn(result.getValue().get(1));
            }
        }

        for (Result result : bytesOut) {
            OperatorMetric current_value = operatorMetrics.get(result.getMetric().getTaskId());
            if (current_value.getBytesOut() < result.getValue().get(1)) {
                current_value.setBytesOut(result.getValue().get(1));
            }
        }

        for (Result result : messagesIn) {
            OperatorMetric current_value = operatorMetrics.get(result.getMetric().getTaskId());
            if (current_value.getRecordsIn() < result.getValue().get(1)) {
                current_value.setRecordsIn(result.getValue().get(1));
            }
        }

        for (Result result : messagesOut) {
            OperatorMetric current_value = operatorMetrics.get(result.getMetric().getTaskId());
            if (current_value.getRecordsOut() < result.getValue().get(1)) {
                current_value.setRecordsOut(result.getValue().get(1));
            }
        }

        for (Result result : backpressure) {
            OperatorMetric current_value = operatorMetrics.get(result.getMetric().getTaskId());
            if (current_value.getBackPresure() < result.getValue().get(1)) {
                current_value.setBackPresure(result.getValue().get(1));
            }
        }

        for (Result result : latency) {
            OperatorMetric current_value = operatorMetrics.get(result.getMetric().getOperatorId());
            if (current_value.getLatency() < result.getValue().get(1)) {
                current_value.setLatency(result.getValue().get(1));
            }
        }

    }

}
