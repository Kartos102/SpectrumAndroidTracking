package com.jo.spectrumtracking.model;

public class Resp_Driver {
    private String _id;
    private String trackerId;
    private String userId;
    private String name;
    private String driverName;
    private String driverPhoneNumber;
    private String color;
    private String autoRenew;

    public Resp_Driver() {
    }

    public Resp_Driver(String _id, String trackerId, String userId, String name, String driverName, String driverPhoneNumber, String color, String autoRenew) {
        this._id = _id;
        this.trackerId = trackerId;
        this.userId = userId;
        this.name = name;
        this.driverName = driverName;
        this.driverPhoneNumber = driverPhoneNumber;
        this.color = color;
        this.autoRenew = autoRenew;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTrackerId() {
        return trackerId;
    }

    public void setTrackerId(String trackerId) {
        this.trackerId = trackerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhoneNumber() {
        return driverPhoneNumber;
    }

    public void setDriverPhoneNumber(String driverPhoneNumber) {
        this.driverPhoneNumber = driverPhoneNumber;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(String autoRenew) {
        this.autoRenew = autoRenew;
    }
}
