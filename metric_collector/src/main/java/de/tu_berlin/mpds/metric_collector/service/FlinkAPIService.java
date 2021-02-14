package de.tu_berlin.mpds.metric_collector.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.model.flinkapi.*;
import de.tu_berlin.mpds.metric_collector.util.FlinkQuery;
import de.tu_berlin.mpds.metric_collector.util.MultiPartBodyPublisher;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service
public class FlinkAPIService {
    @Autowired
    private FlinkQuery flinkQuery;

    protected List<Job> getJobs(HttpClient client, ObjectMapper objectMapper) throws ExecutionException, InterruptedException, JsonProcessingException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(flinkQuery.getFLINK_JOBS_OVERVIEW())).GET().build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.get().body(), JobsResponse.class).getJobs();
    }

    protected Job getJobInfo(String jId, HttpClient client, ObjectMapper objectMapper) throws ExecutionException, InterruptedException, JsonProcessingException, UnsupportedEncodingException {
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

    public List<TaskManager> getTaskManagers(HttpClient client, ObjectMapper objectMapper) throws ExecutionException, InterruptedException, JsonProcessingException {
        HttpRequest request = HttpRequest.newBuilder(URI.create((flinkQuery.getFLINK_TASKMANAGERS()))).GET().build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.get().body(), TaskManagersResponse.class).getTaskManagers();
    }

    public JarUploadResponse postJob(HttpClient client, ObjectMapper objectMapper, String resourcePath) throws  ExecutionException, InterruptedException, JsonProcessingException, FileNotFoundException, IOException {
        Path path = new ClassPathResource(resourcePath).getFile().toPath();
        MultiPartBodyPublisher publisher = new MultiPartBodyPublisher()
                .addPart("jarfile", path);
        HttpRequest request = HttpRequest.newBuilder(URI.create((flinkQuery.getFLINK_JARS_UPLOAD())))
                .header("Content-Type", "multipart/form-data; boundary=" +publisher.getBoundary())
                .POST(publisher.build())
                .build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        JarUploadResponse jarUploadResponse = objectMapper.readValue(response.get().body(), JarUploadResponse.class);
        return jarUploadResponse;
    }

    public JarRunResponse runJar(HttpClient client, ObjectMapper objectMapper, String jarID, String programArgs, String parallelism) throws ExecutionException, InterruptedException, JsonProcessingException {
        JSONObject configuration = new JSONObject();
        configuration.put("programArgs", programArgs);
        configuration.put("parallelism", parallelism);
        HttpRequest request = HttpRequest.newBuilder(URI.create(flinkQuery.getFLINK_JARS_RUN(jarID)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(configuration.toJSONString()))
                .build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        JarRunResponse jarRunResponse = objectMapper.readValue(response.get().body(), JarRunResponse.class);
        return jarRunResponse;
    }

    public JobPlan getJobPlan(HttpClient client, ObjectMapper objectMapper, String jarID, String programArgs) throws ExecutionException, InterruptedException, JsonProcessingException {
        JSONObject configuration = new JSONObject();
        configuration.put("programArgs", programArgs);
        HttpRequest request = HttpRequest.newBuilder(URI.create(flinkQuery.getFLINK_JAR_PLAN(jarID)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(configuration.toJSONString()))
                .build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        JobPlanInfo jobPlanInfo = objectMapper.readValue(response.get().body(), JobPlanInfo.class);
        return jobPlanInfo.getJobPlan();
    }

}
