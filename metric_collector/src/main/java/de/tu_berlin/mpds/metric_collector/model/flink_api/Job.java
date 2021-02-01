package de.tu_berlin.mpds.metric_collector.model.flink_api;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;


@Getter
@Setter
@ToString
public class Job {

  private String jid;
  private String name;
  private String state;
  private List<JobVertex> vertices;
  // private JobPlan plan;

  public Job(@JsonProperty("jid") String id, @JsonProperty("state") String state, @JsonProperty("name") String name,
             @JsonProperty("vertices")List<JobVertex> vertices
             // @JsonProperty("plan") JobPlan plan
  ) {
    this.state = state;
    this.jid = id;
    this.name = name;
    this.vertices = vertices;
    // this.plan = plan;
  }
}
