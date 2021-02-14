package de.tu_berlin.mpds.metric_collector.model.experiments;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

public class RunConfiguration {
    private List<Integer> operator_parallelism;

    public RunConfiguration(List<Integer> operator_parallelism) {
        this.operator_parallelism = operator_parallelism;
    }
}
