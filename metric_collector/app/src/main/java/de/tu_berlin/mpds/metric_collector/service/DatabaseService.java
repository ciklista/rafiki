package de.tu_berlin.mpds.metric_collector.service;

import de.tu_berlin.mpds.metric_collector.configuration.ApplicationConfiguration;
import de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.Job;
import de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.Operator;
import de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.OperatorMetric;
import de.tu_berlin.mpds.metric_collector.model.eperimentmetrics.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseService {
    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://" + applicationConfiguration.getPostgreshost() + ':' +
                        applicationConfiguration.getPostgresport() + '/' + applicationConfiguration.getPostgresdb(),
                applicationConfiguration.getPostgresuser(),
                applicationConfiguration.getPostgrespw());


    }

    public void insertJobs(List<Job> jobs) throws SQLException {
        Connection conn = getConnection();
        for (Job job : jobs) {
            String sql = "INSERT INTO experiments.jobs (job_id,job_name) VALUES (?,?);";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, job.getJobId());
            pstmt.setString(2, job.getJobName());
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
            String sql = "INSERT INTO experiments.operators (operator_id,job_id, task_name, preceding_operator_id, succeeding_operator_id) VALUES (?,?,?,?,?);";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, operator.getOperatorId());
            pstmt.setString(2, operator.getJobId());
            pstmt.setString(3, operator.getTaskName());
            pstmt.setString(4, operator.getPrecedingOperatorId());
            pstmt.setString(5, operator.getSucceedingOperatorId());
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
            String sql = "INSERT INTO experiments.operator_metrics (experiment_id,operator_id,job_id, max_records_in, max_records_out, max_bytes_in, max_bytes_out, max_latency, max_backpresure, operator_parallelism) VALUES (?,?,?,?,?,?, ?, ?,?,?);";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, metric.getExperimentId());
            pstmt.setString(2, metric.getOperatorId());
            pstmt.setString(3, metric.getJobId());
            pstmt.setDouble(4, metric.getRecordsIn());
            pstmt.setDouble(5, metric.getRecordsOut());
            pstmt.setDouble(6, metric.getBytesIn());
            pstmt.setDouble(7, metric.getBytesOut());
            pstmt.setDouble(8, metric.getLatency());
            pstmt.setDouble(9, metric.getBackPresure());
            pstmt.setInt(10, metric.getOperatorParallelism());
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
}


