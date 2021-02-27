package de.tu_berlin.mpds.metric_collector.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "config")
public class ApplicationConfiguration {


    private String clusteraddress;
    private String prometheusapibasepath;
    private String prometheusport;
    private String flinkport;
    private String postgreshost;
    private int postgresport;
    private String postgresuser;
    private String postgrespw;
    private String postgresdb;


    public String getClusteraddress() {
        return clusteraddress;
    }


    public String getPrometheusapibasepath() {
        return prometheusapibasepath;
    }


    public String getPrometheusPort() {
        return prometheusport;
    }

    public String getFlinkPort() {
        return flinkport;
    }


    public String getPostgreshost() {
        return postgreshost;
    }

    public int getPostgresport() {
        return postgresport;
    }

    public String getPostgresuser() {
        return postgresuser;
    }

    public String getPostgrespw() {
        return postgrespw;
    }

    public String getPostgresdb() {
        return postgresdb;
    }

}

