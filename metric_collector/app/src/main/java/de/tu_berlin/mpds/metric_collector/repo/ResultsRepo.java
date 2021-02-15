package de.tu_berlin.mpds.metric_collector.repo;

import de.tu_berlin.mpds.metric_collector.model.db.Results;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultsRepo extends JpaRepository<Results,Long> {
}
