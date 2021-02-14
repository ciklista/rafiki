package de.tu_berlin.mpds.metric_collector.model.flinkapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

public class JobPlanInfo {
    private JobPlan jobPlan;

    public JobPlanInfo(@JsonProperty("plan") JobPlan jobPlan) {
        this.jobPlan = jobPlan;
    }
}
