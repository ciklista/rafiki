package de.tu_berlin.mpds.metric_collector.model.prometheusmetric;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@JsonPropertyOrder({"metric","value"})
@ToString
public class Result {

  private Metric metric;
  private List<Object> value;

  public Result(@JsonProperty("metric") Metric metric,@JsonProperty("value") List<Object> value) {
    this.metric = metric;
    this.value = value;
  }
}