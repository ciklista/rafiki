package de.tu_berlin.mpds.metric_collector.model.eperimentmetrics;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KafkaMetric implements Serializable {

  private long experimentId;
  private long kafkaLag;
  private long kafkaMessagesPerSecond;

  public KafkaMetric(long kafkaLag, long kafkaMessagesPerSecond) {
    this.kafkaLag = kafkaLag;
    this.kafkaMessagesPerSecond = kafkaMessagesPerSecond;
  }
}
