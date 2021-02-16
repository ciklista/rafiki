package de.tu_berlin.mpds.metric_collector.model.eperimentmetrics;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperatorMetric implements Serializable {

  private String experimentId;
  private String operatorId;
  private String jobId;
  private int operatorParallelism;
  private double recordsIn;
  private double recordsOut;
  private double bytesIn;
  private double bytesOut;
  private double latency;
  private double backPresure;

}
