package de.tu_berlin.mpds.metric_collector.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.configuration.FlinkConfig;
import de.tu_berlin.mpds.metric_collector.model.flink_api.Job;
import de.tu_berlin.mpds.metric_collector.model.flink_api.JobVertex;
import de.tu_berlin.mpds.metric_collector.model.flink_api.JobsResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class RequestFlinkAPI {

    private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    //prepare urls with query
    private String queryJobs = FlinkConfig.BASE_URL_FLINK + "/jobs/overview";
    private String queryJob = FlinkConfig.BASE_URL_FLINK + "/jobs/";

    //this method is used for printing the output of the request after the application runs
    //logic may change or new runner class will introduce later.
    //@PostConstruct
    @Scheduled(cron = "0/2 * * * * ?")
    public void init() throws InterruptedException, ExecutionException, IOException {

        //we are printing the response for now!!!
        List<Job> jobs = getJobs(queryJobs);
        System.out.println("Received " + jobs.size() + " jobs:");

        for (Job job : jobs) {
            System.out.println("Job " + job.getName() + ": " + job.getJid() + ": " + job.getState());
            Job job_info = getJobInfo(queryJob + job.getJid());
            System.out.println("The job has the following vertices: ");
            for (JobVertex vertex : job_info.getVertices()) {
                System.out.println("Name: " + vertex.getName());
                System.out.println("Parallelism: " + vertex.getParallelism());
                System.out.println("Status: " + vertex.getStatus());
            }
            System.out.println("------");

        }

    }


    private List<Job> getJobs(String url) throws ExecutionException, InterruptedException, JsonProcessingException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.get().body(), JobsResponse.class).getJobs();
    }

    private Job getJobInfo(String url) throws ExecutionException, InterruptedException, JsonProcessingException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.get().body(), Job.class);
    }

}
