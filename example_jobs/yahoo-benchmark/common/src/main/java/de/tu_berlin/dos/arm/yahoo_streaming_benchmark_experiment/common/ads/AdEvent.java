package de.tu_berlin.dos.arm.yahoo_streaming_benchmark_experiment.common.ads;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.*;

public class AdEvent {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone="UTC")
    private Long event_time;
    private String user_id;
    private String page_id;
    private String ad_id;
    private String ad_type;
    private String event_type;
    private String ip_address;

    public AdEvent(Long event_time, String user_id, String page_id, String ad_id, String ad_type, String event_type, String ip_address) {

        this.event_time = event_time;
        this.user_id = user_id;
        this.page_id = page_id;
        this.ad_id = ad_id;
        this.ad_type = ad_type;
        this.event_type = event_type;
        this.ip_address = ip_address;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPage_id() {
        return page_id;
    }

    public void setPage_id(String page_id) {
        this.page_id = page_id;
    }

    public String getAd_id() {
        return ad_id;
    }

    public void setAd_id(String ad_id) {
        this.ad_id = ad_id;
    }

    public String getAd_type() {
        return ad_type;
    }

    public void setAd_type(String ad_type) {
        this.ad_type = ad_type;
    }

    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public Long getEvent_time() {
        return event_time;
    }

    public void setTimestamp(Long event_time) {
        this.event_time = event_time;
    }

    @Override
    public String toString() {
        return "{event_time:" + event_time +
                ", user_id:" + user_id +
                ", page_id:" + page_id +
                ", ad_id:" + ad_id +
                ", ad_type:" + ad_type +
                ", event_type:" + event_type +
                ", ip_address:" + ip_address +
                '}';
    }
}
