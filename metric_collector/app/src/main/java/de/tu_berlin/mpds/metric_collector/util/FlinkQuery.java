package de.tu_berlin.mpds.metric_collector.util;

import de.tu_berlin.mpds.metric_collector.configuration.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class FlinkQuery {


    private String BASE_URL_FLINK;
    private String FLINK_JOBS_OVERVIEW = "/jobs/overview";
    private String FLINK_JOBS = "/jobs/";
    private String FLINK_TASKMANAGERS = "/taskmanagers";
    private String FLINK_JARS_UPLOAD = "/jars/upload";
    private String FLINK_JARS = "/jars";

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    public String getBASE_URL_FLINK() {
        BASE_URL_FLINK = applicationConfiguration.getClusteraddress() + ":" + applicationConfiguration.getFlinkPort();
        return BASE_URL_FLINK;
    }

    public String getJobsOverview() {
        return getBASE_URL_FLINK() + FLINK_JOBS_OVERVIEW;
    }

    public String getJobs() {
        return getBASE_URL_FLINK() + FLINK_JOBS;
    }

    public String getSubtaskInformation(String job_id, String vertexId) {
        return getBASE_URL_FLINK() + FLINK_JOBS + job_id + "/vertices/" + vertexId;
    }

    public String getJob(String jobId) {
        return getBASE_URL_FLINK() + FLINK_JOBS + jobId;
    }

    public String getTaskmanagers() {
        return getBASE_URL_FLINK() + FLINK_TASKMANAGERS;
    }

    public String getJars() {
        return getBASE_URL_FLINK() + FLINK_JARS_UPLOAD;
    }

    public String getJarsRun(String jarID) {
        return getBASE_URL_FLINK() + FLINK_JARS + "/" + jarID + "/run";
    }

    public String getJarsPlan(String jarID) {
        return getBASE_URL_FLINK() + FLINK_JARS + "/" + jarID + "/plan";
    }
}
