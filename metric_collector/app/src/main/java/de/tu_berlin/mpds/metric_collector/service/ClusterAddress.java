package de.tu_berlin.mpds.metric_collector.service;

import org.springframework.stereotype.Service;

@Service
public class ClusterAddress {

  private String clusterAddress;

  public String getClusterAddress() {
    return clusterAddress;
  }

  public void setClusterAddress(String clusterAddress) {
    this.clusterAddress = clusterAddress;
  }
}
