package de.tu_berlin.mpds.metric_collector.model.prometheusmetric;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonPropertyOrder({"status","data"})
@ToString
public class PrometheusJsonResponse {

  private String status;
  private Data data;

  public PrometheusJsonResponse(@JsonProperty("status") String status,@JsonProperty("data") Data data) {
    this.status = status;
    this.data = data;
  }
}
