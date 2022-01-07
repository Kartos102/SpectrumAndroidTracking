package com.jo.spectrumtracking.model;

/**
 * Created by JO on 3/18/2018.
 */

public class DriverInfo {
    private String driverName;
    private String driverPhone;
    private String vehicleName;

    public DriverInfo(String driverName, String driverPhone, String vehicleName) {
        this.driverName = driverName;
        this.driverPhone = driverPhone;
        this.vehicleName = vehicleName;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }
}
