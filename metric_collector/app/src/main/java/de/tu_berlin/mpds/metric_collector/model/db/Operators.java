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
@Table(name = "operators")
@NoArgsConstructor
@AllArgsConstructor
public class Operators implements Serializable {

  //-- is task id
  @Id
  @Column(name = "operator_id")
  private String operatorId;
  @Column(name = "job_id")
  private int  jobId;
  @Column(name = "task_name")
  private String taskName;
  @Column(name = "preceding_operator_id")
  private int precedingOperatorId;
  @Column(name = "succeeding_operator_id")
  private int succeedingOperatorId;

}
