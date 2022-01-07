package com.jo.spectrumtracking.model;

public class Resp_SharedDevice {
    private String flag;
    private String report_id;
    private String plateNumber;

    public Resp_SharedDevice(String flag, String report_id, String plateNumber) {
        this.flag = flag;
        this.report_id = report_id;
        this.plateNumber = plateNumber;
    }

    public Resp_SharedDevice() {
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
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
}
