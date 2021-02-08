package de.tu_berlin.mpds.metric_collector.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.model.flinkmetric.Job;
import de.tu_berlin.mpds.metric_collector.model.flinkmetric.JobsResponse;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class FlinkAPIMetricService {

    protected List<Job> getJobs(String url,HttpClient client,ObjectMapper objectMapper) throws ExecutionException, InterruptedException, JsonProcessingException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.get().body(), JobsResponse.class).getJobs();
    }

   protected Job getJobInfo(String url,HttpClient client,ObjectMapper objectMapper) throws ExecutionException, InterruptedException, JsonProcessingException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.get().body(), Job.class);
    }

}
