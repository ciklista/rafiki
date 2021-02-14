package de.tu_berlin.mpds.metric_collector.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;




@Configuration
@ConfigurationProperties(prefix = "config")
public class ApplicationConfiguration {



  private String clusteraddress ;

  private  String prometheusapibasepath;

  private  String prometheusport;

  private String flinkport;


 public String getClusteraddress() {
  return clusteraddress;
 }

 public void setClusteraddress(String clusteraddress) {
  this.clusteraddress = clusteraddress;
 }

 public String getPrometheusapibasepath() {
  return prometheusapibasepath;
 }

 public void setPrometheusapibasepath(String prometheusapibasepath) {
  this.prometheusapibasepath = prometheusapibasepath;
 }

 public String getPrometheusPort() {
  return prometheusport;
 }

 public void setPrometheusPort(String prometheusport) {
  this.prometheusport = prometheusport;
 }

 public String getFlinkPort() {
  return flinkport;
 }

 public void setFlinkPort(String fport) {
  this.flinkport = fport;
 }
}

