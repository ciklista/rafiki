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
public class KafkaMetric implements Serializable {

  private String experimentId;
  private double kafkaLag;
  private double kafkaMessagesPerSecond;

}
