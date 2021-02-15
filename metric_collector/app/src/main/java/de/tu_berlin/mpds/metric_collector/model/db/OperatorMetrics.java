package de.tu_berlin.mpds.metric_collector.model.db;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "operator_metrics")
@NoArgsConstructor
public class OperatorMetrics implements Serializable {

  @Id
  @GeneratedValue
  private long experimentId;
  @Column(name= "operator_id")
  private String operatorId;
  @Column(name= "operator_parallelism ")
  private int operatorParallelism;
  @Column(name= "max_records_in")
  private long maxRecordsIn;
  @Column(name= "max_records_out")
  private long maxRecordsOut;
  @Column(name= "max_bytes_in")
  private long maxBytesIn;
  @Column(name= "max_bytes_out")
  private long maxBytesOut;
  @Column(name= "max_latency")
  private long maxLatency;
  @Column(name= "max_backpresure")
  private long maxBackPresure;

  public OperatorMetrics(String operatorId, int operatorParallelism, long maxRecordsIn, long maxRecordsOut, long maxBytesIn, long maxBytesOut, long maxLatency, long maxBackPresure) {
    this.operatorId = operatorId;
    this.operatorParallelism = operatorParallelism;
    this.maxRecordsIn = maxRecordsIn;
    this.maxRecordsOut = maxRecordsOut;
    this.maxBytesIn = maxBytesIn;
    this.maxBytesOut = maxBytesOut;
    this.maxLatency = maxLatency;
    this.maxBackPresure = maxBackPresure;
  }
}
