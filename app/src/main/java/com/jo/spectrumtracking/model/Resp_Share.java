package com.jo.spectrumtracking.model;

public class Resp_Share {
    private String report_id;
    private String plateNumber;
    private String spinner_label;

    public Resp_Share() {
    }

    public Resp_Share(String report_id, String plateNumber, String spinner_label) {
        this.report_id = report_id;
        this.plateNumber = plateNumber;
        this.spinner_label = spinner_label;
    }

    public String getReport_id() {
        return report_id;
    }

    public void setReport_id(String report_id) {
        this.report_id = report_id;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getSpinner_label() {
        return spinner_label;
    }

    public void setSpinner_label(String spinner_label) {
        this.spinner_label = spinner_label;
    }
}
