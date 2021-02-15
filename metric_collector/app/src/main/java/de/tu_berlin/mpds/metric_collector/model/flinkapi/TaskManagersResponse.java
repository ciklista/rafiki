package de.tu_berlin.mpds.metric_collector.model.flinkapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString

public class TaskManagersResponse {

    private List<TaskManager> taskManagers;

    public TaskManagersResponse(@JsonProperty("taskmanagers") List<TaskManager> taskManagers) {
        this.taskManagers = taskManagers;
    }
}
