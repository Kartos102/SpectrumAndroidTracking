package com.jo.spectrumtracking.model;

import java.util.List;

/**
 * Created by JO on 3/17/2018.
 */

public class OrderService {
    private String name;
    private String trackerId;
    private String expirationDate;
    private List<ServicePlan> servicePlanList;
    private List<LTEData> lteDataList;
    private Boolean autoReview;

    private int selectedServicePlanId;
    private int selectedLTEDataId;

    private boolean servicePlanEnabled;
    private boolean lteDataEnabled;


    public OrderService() {
    }

    public OrderService(String name, String trackerId, String expirationDate, List<ServicePlan> servicePlanList, List<LTEData> lteDataList, Boolean autoReview, int selectedServicePlanId, int selectedLTEDataId, boolean servicePlanEnabled, boolean lteDataEnabled) {
        this.name = name;
        this.trackerId = trackerId;
        this.expirationDate = expirationDate;
        this.servicePlanList = servicePlanList;
        this.lteDataList = lteDataList;
        this.autoReview = autoReview;
        this.selectedServicePlanId = selectedServicePlanId;
        this.selectedLTEDataId = selectedLTEDataId;
        this.servicePlanEnabled = servicePlanEnabled;
        this.lteDataEnabled = lteDataEnabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTrackerId() {
        return trackerId;
    }

    public void setTrackerId(String trackerId) {
        this.trackerId = trackerId;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public List<ServicePlan> getServicePlanList() {
        return servicePlanList;
    }

    public void setServicePlanList(List<ServicePlan> servicePlanList) {
        this.servicePlanList = servicePlanList;
    }

    public List<LTEData> getLteDataList() {
        return lteDataList;
    }

    public void setLteDataList(List<LTEData> lteDataList) {
        this.lteDataList = lteDataList;
    }

    public Boolean getAutoReview() {
        return autoReview;
    }

    public void setAutoReview(Boolean autoReview) {
        this.autoReview = autoReview;
    }

    public int getSelectedServicePlanId() {
        return selectedServicePlanId;
    }

    public void setSelectedServicePlanId(int selectedServicePlanId) {
        this.selectedServicePlanId = selectedServicePlanId;
    }

    public int getSelectedLTEDataId() {
        return selectedLTEDataId;
    }

    public void setSelectedLTEDataId(int selectedLTEDataId) {
        this.selectedLTEDataId = selectedLTEDataId;
    }

    public boolean isServicePlanEnabled() {
        return servicePlanEnabled;
    }

    public void setServicePlanEnabled(boolean servicePlanEnabled) {
        this.servicePlanEnabled = servicePlanEnabled;
    }

    public boolean isLteDataEnabled() {
        return lteDataEnabled;
    }

    public void setLteDataEnabled(boolean lteDataEnabled) {
        this.lteDataEnabled = lteDataEnabled;
    }
}
