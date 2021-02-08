package de.tu_berlin.mpds.metric_collector.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.PrometheusJsonResponse;
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

  protected PrometheusJsonResponse  sendRequestToPrometheusForMetric(String urlWithQuery,HttpClient client,ObjectMapper objectMapper ) throws
          InterruptedException, ExecutionException, IOException {

    HttpRequest request = HttpRequest.newBuilder(URI.create(urlWithQuery)).GET().build();

    CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

    return  objectMapper.readValue(response.get().body(),PrometheusJsonResponse.class);
  }

}
