package de.tu_berlin.mpds.metric_collector.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.model.experiments.RunConfiguration;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.JobPlan;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.TaskManager;
import de.tu_berlin.mpds.metric_collector.service.FlinkAPIService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.util.ArrayList;
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
        return maxParallelism;
    }

    public List<String> getJobArgs(HttpClient client, ObjectMapper objectMapper, String[] operatorNames, int testingParallelism, String otherArgs) throws ExecutionException, InterruptedException, JsonProcessingException {
        int maxParallelism = this.getMaxParallelism(client, objectMapper);
        List<String> configs = new ArrayList<>();
        for (int i = 0; i < operatorNames.length; i++) {
            JSONObject parallelismConfiguration = new JSONObject();
            for (int j = 0; j < operatorNames.length; j++) {
                if (j == i) {
                    parallelismConfiguration.put(operatorNames[j], testingParallelism);
                } else {
                    parallelismConfiguration.put(operatorNames[j], maxParallelism);
                }
            }
            configs.add(parallelismConfiguration.toJSONString() + " " + otherArgs);
        }
        return configs;
    }
}
