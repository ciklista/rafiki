package de.tu_berlin.mpds.metric_collector.configuration;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class FlinkConfig extends ClusterConfig {

    //we will change this configuration class by mapping it to properties file.
    public static final String BASE_URL_FLINK = "http://" + CLUSTER_URL + ":30881";
    public static final String FLINK_JOBS_OVERVIEW = "/jobs/overview";
    public static final String FLINK_JOBS = "/jobs/";

}
