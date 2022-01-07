package com.jo.spectrumtracking.model;

public class Resp_Event {
    private String localDateTime;
    private String alarm;
    private String address;

    public Resp_Event(String localDateTime, String alarm, String address) {
        this.localDateTime = localDateTime;
        this.alarm = alarm;
        this.address = address;
    }

    public String getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(String localDateTime) {
        this.localDateTime = localDateTime;
    }

    public String getAlarm() {
        return alarm;
    }

    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
