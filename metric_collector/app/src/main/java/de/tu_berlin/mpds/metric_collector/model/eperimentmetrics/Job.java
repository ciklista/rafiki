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
public class Job implements Serializable {

  private String jobId;
  private String jobName;
  private String jarId;
}
