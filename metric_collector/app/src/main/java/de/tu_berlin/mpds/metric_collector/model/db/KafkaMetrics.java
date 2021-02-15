package de.tu_berlin.mpds.metric_collector.model.db;

import com.fasterxml.jackson.annotation.JsonTypeId;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "kafka_metrics")
@NoArgsConstructor
public class KafkaMetrics implements Serializable {

  @Id
  @GeneratedValue
  private long experimentId;
  @Column(name = "max_kafka_lag",nullable = false)
  private long maxKafkaLag;
  @Column(name = "mac_kafka_messages_per_second",nullable = false)
  private long maxKafkaMessagesPerSecond;

  public KafkaMetrics(long maxKafkaLag, long maxKafkaMessagesPerSecond) {
    this.maxKafkaLag = maxKafkaLag;
    this.maxKafkaMessagesPerSecond = maxKafkaMessagesPerSecond;
  }
}
