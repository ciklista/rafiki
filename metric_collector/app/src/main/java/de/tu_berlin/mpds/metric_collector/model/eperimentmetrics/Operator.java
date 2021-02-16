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
public class Operator implements Serializable {

  //-- is task id
  private String operatorId;
  private String jobId;
  private String taskName;
  private String precedingOperatorId;
  private String succeedingOperatorId;

}
