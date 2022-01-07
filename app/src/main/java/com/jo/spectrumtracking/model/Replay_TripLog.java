package com.jo.spectrumtracking.model;

public class Replay_TripLog {
    private String trip_index;
    private String range;
    private String detail;
    private double maxSpeed;
    private int state;

    public Replay_TripLog(String trip_index, String range, String detail, int state, double maxspeed) {
        this.trip_index = trip_index;
        this.range = range;
        this.detail = detail;
        this.state = state;
        this.maxSpeed = maxspeed;
    }

    public String getTrip_index() {
        return trip_index;
    }

    public void setTrip_index(String trip_index) {
        this.trip_index = trip_index;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
