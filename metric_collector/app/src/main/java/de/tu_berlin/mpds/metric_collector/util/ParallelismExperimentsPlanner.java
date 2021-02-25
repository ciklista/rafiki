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
import java.util.Arrays;
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

    public String getNextJobArgs(int maxParallelism, String[] operatorNames, int[] previousParallelismConfig, int lastBackpressureOperator, String otherArgs) {
        if (previousParallelismConfig == null) {
            int[] array = new int[operatorNames.length];
            Arrays.fill(array, 1);
            return createArgString(generateArgJson(operatorNames, array), otherArgs);
        }
        else if (lastBackpressureOperator == -1){
            previousParallelismConfig[0] += 1;
            return createArgString(generateArgJson(operatorNames, previousParallelismConfig), otherArgs);
        }
        else {
            previousParallelismConfig[lastBackpressureOperator +1] += 1;
            return createArgString(generateArgJson(operatorNames, previousParallelismConfig), otherArgs);
        }

    }

    private JSONObject generateArgJson(String[] operatorNames, int[] parallelism) {
        JSONObject parallelismConfiguration = new JSONObject();
        for (int j = 0; j < operatorNames.length; j++) {
            parallelismConfiguration.put(operatorNames[j], parallelism[j]);
        }
        return parallelismConfiguration;
    }

    private String createArgString(JSONObject operatorConfig, String otherArgs) {
        return operatorConfig.toJSONString() + " " + otherArgs;
    }

}
