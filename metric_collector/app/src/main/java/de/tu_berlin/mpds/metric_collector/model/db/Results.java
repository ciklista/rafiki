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
@Table(name = "results")
@NoArgsConstructor
public class Results implements Serializable {

  @Id
  @GeneratedValue
  private long experiment_id;
  @Column(name="job_id" )
  private int jobId;
  @Column(name="start_timestamp" )
  private long startTimestamp;
  @Column(name="end_timestamp" )
  private long endTimestamp;

  public Results(int jobId, long startTimestamp, long endTimestamp) {
    this.jobId = jobId;
    this.startTimestamp = startTimestamp;
    this.endTimestamp = endTimestamp;
  }
}
