package de.tu_berlin.mpds.metric_collector.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.model.flinkmetric.Job;
import de.tu_berlin.mpds.metric_collector.model.flinkmetric.JobSubtask;
import de.tu_berlin.mpds.metric_collector.model.flinkmetric.JobVertex;
import de.tu_berlin.mpds.metric_collector.model.flinkmetric.JobsResponse;
import de.tu_berlin.mpds.metric_collector.util.FlinkQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service
public class FlinkAPIMetricService {
    @Autowired
    private FlinkQuery flinkQuery;

    protected List<Job> getJobs(HttpClient client, ObjectMapper objectMapper) throws ExecutionException, InterruptedException, JsonProcessingException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(flinkQuery.getFLINK_JOBS_OVERVIEW())).GET().build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        String responseString = response.get().body();
        return objectMapper.readValue(response.get().body(), JobsResponse.class).getJobs();
    }

    protected Job getJobInfo(String jId, HttpClient client, ObjectMapper objectMapper) throws ExecutionException, InterruptedException, JsonProcessingException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(flinkQuery.getFLINK_JOBS() + jId)).GET().build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        Job job = objectMapper.readValue(response.get().body(), Job.class);
        // append subTask information
        List<JobVertex> verticesWithSubtasks =  new ArrayList<>();
        for (JobVertex vertex : job.getVertices()) {
            request = HttpRequest.newBuilder(URI.create(flinkQuery.getFLINK_SUBTASK_INFORMATION(job.getJid(), vertex.getId()))).GET().build();
            response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            verticesWithSubtasks.add(objectMapper.readValue(response.get().body(), JobVertex.class));
        }
        job.setVertices(verticesWithSubtasks);
        return job;
    }

}
