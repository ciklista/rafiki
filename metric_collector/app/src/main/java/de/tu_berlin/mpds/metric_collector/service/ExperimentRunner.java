package de.tu_berlin.mpds.metric_collector.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.Job;
import de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.Operator;
import de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.OperatorMetric;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.JarRunResponse;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.JobVertex;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.Result;
import de.tu_berlin.mpds.metric_collector.util.FlinkQuery;
import de.tu_berlin.mpds.metric_collector.util.ParallelismExperimentsPlanner;
import de.tu_berlin.mpds.metric_collector.util.PrometheusQuery;

import java.sql.SQLException;
import java.util.List;

import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class ExperimentRunner {
    @Getter
    @Setter
    @AllArgsConstructor
    private static class ExperimentResult {
        public final int[] parallelismConfig;
        public final int lastBackpressuredOperator;
    }

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


    public void start(String[] operators,String appJarId, int maximumParallelism) throws InterruptedException, ExecutionException, IOException, SQLException {
        String[] operatorNames = operators;
        int maxParallelism = maximumParallelism;
        int lastBackpressuredOperator = -1;
        int[] operatorConfig = null;
        boolean nextExperiment = true;
        String jarId = appJarId;
        while (nextExperiment) {
            String jobArg = experimentPlanner.getNextJobArgs(operatorNames, operatorConfig, lastBackpressuredOperator, "1000");

            ExperimentResult result = run_experiment(jarId, jobArg, 60);

            operatorConfig = result.getParallelismConfig();
            lastBackpressuredOperator = result.getLastBackpressuredOperator();

            if (operatorConfig[lastBackpressuredOperator + 1] == maxParallelism) {
                nextExperiment = false;
            }
        }
    }

    private ExperimentResult run_experiment(String jarID, String programArgs, int experimentDuration) throws InterruptedException, ExecutionException, IOException, SQLException {
        // start job with given config
        JarRunResponse response = flinkAPIService.runJar(client, objectMapper, jarID, programArgs, "1");
        String jobId = response.getJobID();
        de.tu_berlin.mpds.metric_collector.model.flinkapi.Job jobInfo = flinkAPIService.getJobInfo(jobId, client, objectMapper);
        List<JobVertex> jobVertices = jobInfo.getVertices();
        StringBuilder parallelismPrintString = new StringBuilder();
        for (JobVertex vertex : jobVertices) {
            parallelismPrintString.append("\t").append(vertex.getName()).append(" - Parallelism: ").append(vertex.getParallelism()).append("\n");
        }
        System.out.println("Started job " + jobId + " with the following parallelism config:");
        System.out.println(parallelismPrintString);
        System.out.println("Waiting 10 seconds for the job to settle in");
        Thread.sleep(10 * 1000);

        // generate experiment_id
        String experimentId = UUID.randomUUID().toString();



        // run experiment + collect metrics
        long experimentStarted = System.currentTimeMillis() / 1000L;

        HashMap<String, OperatorMetric> maxOperatorMetrics = runJobAndCollectMetrics(experimentDuration, jobId, experimentId, jobVertices);

        long experimentStopped = System.currentTimeMillis() / 1000L;

        System.out.println("Experiment done.");
        flinkAPIService.cancelJob(client, objectMapper, jobId);
        System.out.println("Cancelled job.");


        // update db
        Job job = new Job(jobId, jobInfo.getName(), jarID);

        List<Operator> jobOperators = new ArrayList<>();
        for (JobVertex vertex : jobVertices) {
            jobOperators.add(new Operator(vertex.getId(), jobId, vertex.getName(), vertex.getTaskPosition()));
        }

        de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.Result experimentResult = new de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.Result(experimentId, jobId, experimentStarted, experimentStopped);

        List<OperatorMetric> operatorMetricList = new ArrayList<>(maxOperatorMetrics.values());

        System.out.println("Sending results to database...");
        databaseService.insertJobs(job);
        databaseService.insertOperators(jobOperators);
        databaseService.insertResults(experimentResult);
        databaseService.insertOperatorMetrics(operatorMetricList);
        System.out.println("...done.");

        int[] parallelismConfig = new int[jobVertices.size()];
        for (int i = 0; i < jobVertices.size(); i++) {
            parallelismConfig[i] = jobVertices.get(i).getParallelism();
        }
        int lastBackpressuredOperator = -1;
        for (OperatorMetric operatorMetric : operatorMetricList) {
            if (operatorMetric.getBackPresure() > 0.5) {
                for (Operator operator : jobOperators) {
                    if (operator.getOperatorId().equals(operatorMetric.getOperatorId())) {
                        System.out.println("Found backpressure on task: " + operator.getTaskName() + " (Position " + operator.getOperatorPosition() + ")");
                        if (operator.getOperatorPosition() > lastBackpressuredOperator) {
                            lastBackpressuredOperator = operator.getOperatorPosition();
                        }

                    }
                }

            }
        }
        return new ExperimentResult(parallelismConfig, lastBackpressuredOperator);

    }

    private HashMap<String, OperatorMetric> runJobAndCollectMetrics(int durationSec, String jobId, String experimentId, List<JobVertex> vertices) throws InterruptedException, ExecutionException, IOException {
        HashMap<String, OperatorMetric> maxOperatorMetrics = new HashMap<>();
        for (JobVertex vertex : vertices) {
            maxOperatorMetrics.put(vertex.getId(), new OperatorMetric(experimentId, vertex.getId(), jobId, vertex.getParallelism(), 0.0, 0.0, 0.0, 0.0, 0.0, Double.POSITIVE_INFINITY, 0.0));
        }
        System.out.println("Starting metric collection...");
        for (long stop = System.nanoTime() + TimeUnit.SECONDS.toNanos(durationSec); stop > System.nanoTime(); ) {
            updateOperatorMetricsForJob(client, objectMapper, jobId, maxOperatorMetrics);
        }
        System.out.println("Finished experiment for job: " + jobId);
        return maxOperatorMetrics;
    }

    private void updateOperatorMetricsForJob(HttpClient client, ObjectMapper objectMapper, String jobId, HashMap<String, OperatorMetric> operatorMetrics)
        throws InterruptedException, ExecutionException, IOException {

        List<Result> bytesIn = prometheusMetricService.executePrometheusQuery(prometheusQuery.getBytesInByTask(jobId), client, objectMapper).getData().getResult();
        List<Result> bytesOut = prometheusMetricService.executePrometheusQuery(prometheusQuery.getBytesOutByTask(jobId), client, objectMapper).getData().getResult();
        List<Result> messagesIn = prometheusMetricService.executePrometheusQuery(prometheusQuery.getMessagesInByTask(jobId), client, objectMapper).getData().getResult();
        List<Result> messagesOut = prometheusMetricService.executePrometheusQuery(prometheusQuery.getMessagesOutByTask(jobId), client, objectMapper).getData().getResult();
        List<Result> backpressure = prometheusMetricService.executePrometheusQuery(prometheusQuery.getMaxBackpressureByTask(jobId), client, objectMapper).getData().getResult();
        List<Result> latency = prometheusMetricService.executePrometheusQuery(prometheusQuery.getAvgLatencyByTask(jobId), client, objectMapper).getData().getResult();

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
            if (current_value.getMaxLatency() < result.getValue().get(1)) {
                current_value.setMaxLatency(result.getValue().get(1));
            }
            if (current_value.getMinLatency() > result.getValue().get(1) && result.getValue().get(1) != 0.0) {
                current_value.setMinLatency(result.getValue().get(1));
            }
        }

    }

}