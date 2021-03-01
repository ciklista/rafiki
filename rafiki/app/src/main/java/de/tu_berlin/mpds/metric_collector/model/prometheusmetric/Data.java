package de.tu_berlin.mpds.metric_collector.model.prometheusmetric;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@JsonPropertyOrder({"resultType","vector"})
@ToString
public class Data {

  private String resultType;
  private List<Result> result;

  public Data(@JsonProperty("resultType") String resultType, @JsonProperty("vector") List<Result> result) {
    this.resultType = resultType;
    this.result = result;
  }
}
