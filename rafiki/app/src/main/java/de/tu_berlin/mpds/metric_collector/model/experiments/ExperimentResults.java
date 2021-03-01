package de.tu_berlin.mpds.metric_collector.model.experiments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExperimentResults {
    private String taskName;
    private int operatorParallelism;
    private Integer[] maxThroughputArray;
    private int avgMaxThroughput;
    private int highestMaxThroughput;
    private int operatorPosition;
    private boolean backpressureConditionHolds;
}
