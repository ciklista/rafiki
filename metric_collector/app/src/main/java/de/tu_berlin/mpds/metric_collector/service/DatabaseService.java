package de.tu_berlin.mpds.metric_collector.service;

import de.tu_berlin.mpds.metric_collector.configuration.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;
import java.util.HashMap;
import java.util.List;

public class DatabaseService {
    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(applicationConfiguration.getPostgreshost() + ':' +
                        applicationConfiguration.getFlinkPort() + '/' + applicationConfiguration.getPostgresdb(),
                applicationConfiguration.getPostgresuser(),
                applicationConfiguration.getPostgrespw());


    }

    public void insertRows(List<HashMap> jobs) throws SQLException {
        Connection conn = getConnection();
        for (HashMap job: jobs){
            String sql = "INSERT INTO experiments.jobs (job_id,job_name) VALUES (?,?);";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, (int) job.get("job_id"));
            pstmt.setString(2, (String) job.get("job_name"));
            pstmt.executeUpdate();
        }

    }
}


