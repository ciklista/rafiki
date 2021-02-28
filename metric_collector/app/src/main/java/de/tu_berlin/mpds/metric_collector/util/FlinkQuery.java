package de.tu_berlin.mpds.metric_collector.util;

import de.tu_berlin.mpds.metric_collector.configuration.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class FlinkQuery {


    private String FLINK_JOBS_OVERVIEW = "/jobs/overview";
    private String FLINK_JOBS = "/jobs/";
    private String FLINK_TASKMANAGERS = "/taskmanagers";
    private String FLINK_JARS_UPLOAD = "/jars/upload";
    private String FLINK_JARS = "/jars";

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    public String getBaseUrl(String clusterAddress) {
        return clusterAddress + ":" + applicationConfiguration.getFlinkPort();
    }

    public String getJobsOverview(String clusterAddress) {
        return getBaseUrl(clusterAddress) + FLINK_JOBS_OVERVIEW;
    }

    public String getJobs(String clusterAddress) {

        return getBaseUrl(clusterAddress) + FLINK_JOBS;
    }

    public String getSubtaskInformation(String clusterAddress, String job_id, String vertexId) {
        return getBaseUrl(clusterAddress) + FLINK_JOBS + job_id + "/vertices/" + vertexId;
    }

    public String getJob(String clusterAddress, String jobId) {
        return getBaseUrl(clusterAddress) + FLINK_JOBS + jobId;
    }

    public String getTaskmanagers(String clusterAddress) {
        return getBaseUrl(clusterAddress) + FLINK_TASKMANAGERS;
    }

    public String getJars(String clusterAddress) {
        return getBaseUrl(clusterAddress) + FLINK_JARS_UPLOAD;
    }

    public String getJarsRun(String clusterAddress, String jarID) {
        return getBaseUrl(clusterAddress) + FLINK_JARS + "/" + jarID + "/run";
    }

    public String getJarsPlan(String clusterAddress, String jarID) {
        return getBaseUrl(clusterAddress) + FLINK_JARS + "/" + jarID + "/plan";
    }
}
