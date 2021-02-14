package de.tu_berlin.mpds.metric_collector.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.model.experiments.RunConfiguration;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.JobPlan;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.TaskManager;
import de.tu_berlin.mpds.metric_collector.service.FlinkAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class ParallelismExperimentsPlanner {

    @Autowired
    private FlinkQuery flinkQuery;
    @Autowired
    private FlinkAPIService flinkAPIService;

    public int getMaxParallelism(HttpClient client, ObjectMapper objectMapper) throws ExecutionException, InterruptedException, JsonProcessingException {
        List<TaskManager> taskManagers = flinkAPIService.getTaskManagers(client, objectMapper);
        int maxParallelism = 0;
        for (TaskManager taskManager : taskManagers) {
            maxParallelism += taskManager.getSlotsNumber();
        }
        return  maxParallelism;
    }

    public List<RunConfiguration> getExperimentConfigurations(HttpClient client, ObjectMapper objectMapper, String jarID) throws ExecutionException, InterruptedException, JsonProcessingException {
        int maxParallelism = this.getMaxParallelism(client, objectMapper);
        JobPlan jobPlan = flinkAPIService.getJobPlan(client, objectMapper, jarID, "5000");
        int nodeToTest = 0;
        int numberOfNodes = jobPlan.getJobPlanNodes().length;
        List<RunConfiguration> runConfigurations = new LinkedList<>();

        for (int i = 0; i<numberOfNodes; i++) {
            List<Integer> configuration = new LinkedList<>();

            for (int j = 0; j<numberOfNodes; j++) {
                if (j == nodeToTest) {
                    configuration.add(1);
                } else {
                    configuration.add(maxParallelism);
                }
            }
            runConfigurations.add(new RunConfiguration(configuration));
            nodeToTest++;
        }

        return runConfigurations;
    }
}
