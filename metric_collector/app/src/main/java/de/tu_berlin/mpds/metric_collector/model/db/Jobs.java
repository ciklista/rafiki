package de.tu_berlin.mpds.metric_collector.model.db;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "jobs")
@NoArgsConstructor
@AllArgsConstructor
public class Jobs implements Serializable {

  @Id
  @Column(name = "job_id", nullable = false)
  private int jobId;
  @Column(name = "job_name", nullable = false)
  private String jobName;
}
