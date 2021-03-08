package de.tu_berlin.mpds.metric_collector.service;

import de.tu_berlin.mpds.metric_collector.configuration.ApplicationConfiguration;
import de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.Job;
import de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.KafkaMetric;
import de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.Operator;
import de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.OperatorMetric;
import de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.Result;
import de.tu_berlin.mpds.metric_collector.model.experiments.ExperimentResults;
import io.micrometer.core.instrument.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseService {
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private ResourceLoader resourceLoader;
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://" + applicationConfiguration.getPostgreshost() + ':' +
                        applicationConfiguration.getPostgresport() + '/' + applicationConfiguration.getPostgresdb(),
                applicationConfiguration.getPostgresuser(),
                applicationConfiguration.getPostgrespw());


    }

    public void insertJobs(List<Job> jobs) throws SQLException {
        Connection conn = getConnection();
        for (Job job : jobs) {
            String sql = "INSERT INTO experiments.jobs (job_id,job_name,jar_id) VALUES (?,?,?);";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, job.getJobId());
            pstmt.setString(2, job.getJobName());
            pstmt.setString(3, job.getJarId());
            pstmt.executeUpdate();
        }

    }

    public void insertJobs(Job jobs) throws SQLException {
        List<Job> objList = new ArrayList<>();
        objList.add(jobs);
        insertJobs(objList);
    }

    public void insertOperators(List<Operator> operators) throws SQLException {
        Connection conn = getConnection();
        for (Operator operator : operators) {
            String sql = "INSERT INTO experiments.tasks (task_id,job_id, task_name, task_position) VALUES (?,?,?,?);";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, operator.getOperatorId());
            pstmt.setString(2, operator.getJobId());
            pstmt.setString(3, operator.getTaskName());
            pstmt.setInt(4, operator.getOperatorPosition());
            pstmt.executeUpdate();
        }
    }

    public void insertOperators(Operator operator) throws SQLException {
        List<Operator> objList = new ArrayList<>();
        objList.add(operator);
        insertOperators(objList);
    }

    public void insertOperatorMetrics(List<OperatorMetric> metrics) throws SQLException {
        Connection conn = getConnection();
        for (OperatorMetric metric : metrics) {
            String sql = "INSERT INTO experiments.metrics (experiment_id,task_id,job_id, max_records_in, max_records_out, max_bytes_in, max_bytes_out, max_latency, min_latency, max_backpresure, task_parallelism) VALUES (?,?,?,?,?,?,?, ?, ?,?,?);";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, metric.getExperimentId());
            pstmt.setString(2, metric.getOperatorId());
            pstmt.setString(3, metric.getJobId());
            pstmt.setDouble(4, metric.getRecordsIn());
            pstmt.setDouble(5, metric.getRecordsOut());
            pstmt.setDouble(6, metric.getBytesIn());
            pstmt.setDouble(7, metric.getBytesOut());
            pstmt.setDouble(8, metric.getMaxLatency());
            double minLatency = metric.getMinLatency();
            if (minLatency == Double.POSITIVE_INFINITY) {
                minLatency = 0.0;
            }
            pstmt.setDouble(9, minLatency);
            pstmt.setDouble(10, metric.getBackPresure());
            pstmt.setInt(11, metric.getOperatorParallelism());
            pstmt.executeUpdate();
        }
    }

    public void insertOperatorMetrics(OperatorMetric metric) throws SQLException {
        List<OperatorMetric> objList = new ArrayList<>();
        objList.add(metric);
        insertOperatorMetrics(objList);
    }

    public void insertResults(List<Result> results) throws SQLException {
        Connection conn = getConnection();
        for (Result result : results) {
            String sql = "INSERT INTO experiments.results (experiment_id,job_id, start_timestamp, end_timestamp) VALUES (?,?,?,?);";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, result.getExperimentId());
            pstmt.setString(2, result.getJobId());
            pstmt.setLong(3, result.getStartTimestamp());
            pstmt.setLong(4, result.getEndTimestamp());
            pstmt.executeUpdate();
        }
    }

    public void insertResults(Result results) throws SQLException {
        List<Result> resultsList = new ArrayList<>();
        resultsList.add(results);
        insertResults(resultsList);
    }


    public List<ExperimentResults> getExperimentResults(String jarId) throws SQLException, IOException {
        List<ExperimentResults> resultList = new ArrayList<>();
        Connection conn = getConnection();
        Resource resource = resourceLoader.getResource("classpath:experiment_results.sql");
        InputStream inputStream = resource.getInputStream();
        String query = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, jarId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            ExperimentResults result = new ExperimentResults(rs.getString("task_name"),
                    rs.getInt("task_parallelism"),
                    (Integer[]) rs.getArray("max_throughput").getArray(),
                    rs.getInt("avg_max_throughput"),
                    rs.getInt("highest_max_throughput"),
                    rs.getInt("task_position"),
                    rs.getBoolean("backpressure_condition_holds"));
            resultList.add(result);
        }
        return resultList;
    }


}


