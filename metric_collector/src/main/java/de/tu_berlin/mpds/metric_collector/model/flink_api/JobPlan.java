package de.tu_berlin.mpds.metric_collector.model.flink_api;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class JobPlan {

  private String id;
  private String status;

  public JobPlan(@JsonProperty("id") String id, @JsonProperty("status") String status) {
    this.status = status;
    this.id = id;
  }
}
