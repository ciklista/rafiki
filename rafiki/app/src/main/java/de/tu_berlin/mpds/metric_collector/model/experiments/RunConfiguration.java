package de.tu_berlin.mpds.metric_collector.model.experiments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RunConfiguration {
    private String operator_name;
    private int operator_parallelism;
}
