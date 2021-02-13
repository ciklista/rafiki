package de.tu_berlin.mpds.metric_collector.util;

import de.tu_berlin.mpds.metric_collector.configuration.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class FlinkQuery {


  private String BASE_URL_FLINK;
  private String FLINK_JOBS_OVERVIEW =  "/jobs/overview";
  private String FLINK_JOBS = "/jobs/";


  @Autowired
  private ApplicationConfiguration applicationConfiguration;

  public String getBASE_URL_FLINK() {
    BASE_URL_FLINK = applicationConfiguration.getClusteraddress() + ":" + applicationConfiguration.getFlinkPort();

    return BASE_URL_FLINK;
  }

  public String getFLINK_JOBS_OVERVIEW() {
    return getBASE_URL_FLINK() + FLINK_JOBS_OVERVIEW;
  }

  public String getFLINK_JOBS() {
    return getBASE_URL_FLINK() + FLINK_JOBS;
  }

  public String getFLINK_SUBTASK_INFORMATION(String job_id, String vertexId) {
    return getBASE_URL_FLINK() + FLINK_JOBS + job_id + "/vertices/" + vertexId;
  }
}
