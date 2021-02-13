package de.tu_berlin.dos.arm.yahoo_streaming_benchmark_experiment.processor;

public class AdEvent {

    private String user_id;
    private String page_id;
    private String ad_id;
    private String ad_type;
    private String event_type;
    private long event_time;
    private String ip_address;

    public AdEvent() { }

    public AdEvent(String user_id, String page_id, String ad_id, String ad_type, String event_type, long event_time, String ip_address) {
        this.user_id = user_id;
        this.page_id = page_id;
        this.ad_id = ad_id;
        this.ad_type = ad_type;
        this.event_type = event_type;
        this.event_time = event_time;
        this.ip_address = ip_address;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setPage_id(String page_id) {
        this.page_id = page_id;
    }

    public void setAd_id(String ad_id) {
        this.ad_id = ad_id;
    }

    public void setAd_type(String ad_type) {
        this.ad_type = ad_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public void setEvent_time(long event_time) {
        this.event_time = event_time;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getPage_id() {
        return page_id;
    }

    public String getAd_id() {
        return ad_id;
    }

    public String getAd_type() {
        return ad_type;
    }

    public String getEvent_type() {
        return event_type;
    }

    public long getEvent_time() {
        return event_time;
    }

    public String getIp_address() {
        return ip_address;
    }

    @Override
    public String toString() {
        return "AdEvent{" +
                "user_id=" + user_id +
                ", page_id=" + page_id +
                ", ad_id=" + ad_id +
                ", ad_type='" + ad_type + '\'' +
                ", event_type='" + event_type + '\'' +
                ", event_time=" + event_time +
                ", ip_address='" + ip_address + '\'' +
                '}';
    }
}
