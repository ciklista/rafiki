package de.tu_berlin.mpds.metric_collector.repo;

import de.tu_berlin.mpds.metric_collector.model.db.OperatorMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperatorMetricsRepo extends JpaRepository<OperatorMetrics,Long> {
}
