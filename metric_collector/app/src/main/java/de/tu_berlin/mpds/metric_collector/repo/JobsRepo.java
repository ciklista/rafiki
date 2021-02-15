package de.tu_berlin.mpds.metric_collector.repo;

import de.tu_berlin.mpds.metric_collector.model.db.Jobs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobsRepo extends JpaRepository<Jobs,Integer> {
}
