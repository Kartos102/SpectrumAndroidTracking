package com.jo.spectrumtracking.model;

/**
 * Created by JO on 3/18/2018.
 */

public class ReplayRightPanelItem {
    private String driverPhone;
    private String vehicleName;
    private Boolean isSelected;

    public ReplayRightPanelItem(String driverPhone, String vehicleName, Boolean isSelected) {
        this.driverPhone = driverPhone;
        this.vehicleName = vehicleName;
        this.isSelected = isSelected;
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

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }
}
