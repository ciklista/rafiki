package de.tu_berlin.mpds.metric_collector.model.prometheusmetric;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonPropertyOrder({"__name__", "app", "component", "host","instance","job","kubernetes_namespace",
        "kubernetes_pod_name","pod_template_hash","tm_id"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class Metric {

  private String name;
  private String app;
  private String component;
  private String host;
  private String instance;
  private String job;
  private String kubernetesNamespace;
  private String kubernetesPodName;
  private String taskId;
  private String podTemplateHash;
  private String tmId;

  @JsonCreator
  public Metric(@JsonProperty("__name__") String name,@JsonProperty("app") String app, @JsonProperty("component") String component,
                @JsonProperty("host") String host, @JsonProperty("instance") String instance,@JsonProperty("job") String job,
                @JsonProperty("kubernetes_namespace") String kubernetesNamespace,@JsonProperty("kubernetes_pod_name") String kubernetesPodName,
                @JsonProperty("pod_template_hash") String podTemplateHash,@JsonProperty("tm_id") String tmId,
                @JsonProperty("task_id") String task_id) {
    this.name = name;
    this.app = app;
    this.component = component;
    this.host = host;
    this.instance = instance;
    this.job = job;
    this.kubernetesNamespace = kubernetesNamespace;
    this.kubernetesPodName = kubernetesPodName;
    this.podTemplateHash = podTemplateHash;
    this.tmId = tmId;
    this.taskId = task_id;
  }
}
