package de.tu_berlin.mpds.metric_collector.repo;

import de.tu_berlin.mpds.metric_collector.model.db.Operators;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperatorMetricRepo extends JpaRepository<Operators,Integer> {
}
