package de.tu_berlin.mpds.metric_collector.model.eperimentmetrics;

import java.io.Serializable;
import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Result implements Serializable {

  private String experimentId;
  private String jobId;
  private long startTimestamp;
  private long endTimestamp;

}
