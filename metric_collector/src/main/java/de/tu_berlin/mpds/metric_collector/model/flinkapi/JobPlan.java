package de.tu_berlin.mpds.metric_collector.model.flinkapi;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class JobPlan {

  private String jid;
  private String name;
  private JobPlanNode[] jobPlanNodes;

  public JobPlan(@JsonProperty("jid") String jid, @JsonProperty("name") String name, @JsonProperty("nodes") JobPlanNode[] jobPlanNodes) {
    this.name = name;
    this.jid = jid;
    this.jobPlanNodes = jobPlanNodes;
  }
}
