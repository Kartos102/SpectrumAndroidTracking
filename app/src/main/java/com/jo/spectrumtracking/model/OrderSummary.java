package com.jo.spectrumtracking.model;

public class OrderSummary {
    private String vehicle;
    private String tracker;

    private String dataPlan;
    private String LTEData;
    private String dateTd;
    private String autoRenew;

    public OrderSummary(String vehicle, String tracker, String dataPlan, String LTEData, String dateTd, String autoRenew) {
        this.vehicle = vehicle;
        this.tracker = tracker;
        this.dataPlan = dataPlan;
        this.LTEData = LTEData;
        this.dateTd = dateTd;
        this.autoRenew = autoRenew;
    }

    public OrderSummary() {
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getTracker() {
        return tracker;
    }

    public void setTracker(String tracker) {
        this.tracker = tracker;
    }

    public String getDataPlan() {
        return dataPlan;
    }

    public void setDataPlan(String dataPlan) {
        this.dataPlan = dataPlan;
    }

    public String getLTEData() {
        return LTEData;
    }

    public void setLTEData(String LTEData) {
        this.LTEData = LTEData;
    }

    public String getDateTd() {
        return dateTd;
    }

    public void setDateTd(String dateTd) {
        this.dateTd = dateTd;
    }

    public String getAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(String autoRenew) {
        this.autoRenew = autoRenew;
    }
}
