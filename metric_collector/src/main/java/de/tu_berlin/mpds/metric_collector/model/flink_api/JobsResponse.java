package de.tu_berlin.mpds.metric_collector.model.flink_api;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter
@Setter
@ToString
public class JobsResponse {

  private List<Job> jobs;

  public JobsResponse(@JsonProperty("jobs") List<Job> jobs) {
    this.jobs = jobs;
  }
}
