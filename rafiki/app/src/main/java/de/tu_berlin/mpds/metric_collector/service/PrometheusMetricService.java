package de.tu_berlin.mpds.metric_collector.service;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.PrometheusJsonResponse;
import de.tu_berlin.mpds.metric_collector.util.PrometheusQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service
public class PrometheusMetricService {
    @Autowired
    private PrometheusQuery prometheusQuery;
    private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

   PrometheusJsonResponse executePrometheusQuery(URI uri) throws
            InterruptedException, ExecutionException, IOException {

        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.get().body(), PrometheusJsonResponse.class);
    }

}


