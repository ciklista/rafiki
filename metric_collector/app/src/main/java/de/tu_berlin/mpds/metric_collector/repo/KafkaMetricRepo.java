package de.tu_berlin.mpds.metric_collector.repo;

import de.tu_berlin.mpds.metric_collector.model.db.KafkaMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KafkaMetricRepo extends JpaRepository<KafkaMetrics,Long> {
}
