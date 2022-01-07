package com.jo.spectrumtracking.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JO on 3/18/2018.
 */

public class ServicePlan {
    private String servicePlan;
    private double price;
    private List<String> planDetail;

    public ServicePlan(String servicePlan, double price) {
        this.servicePlan = servicePlan;
        this.price = price;
        this.planDetail = new ArrayList<>();
    }

    public ServicePlan(String servicePlan, double price, List<String> planDetail) {
        this.servicePlan = servicePlan;
        this.price = price;

        if (planDetail == null) {
            this.planDetail = new ArrayList<>();
        } else {
            this.planDetail = planDetail;
        }
    }

    public String getServicePlan() {
        return servicePlan;
    }

    public void setServicePlan(String servicePlan) {
        this.servicePlan = servicePlan;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<String> getPlanDetail() {
        return planDetail;
    }

    public void setPlanDetail(List<String> planDetail) {
        this.planDetail = planDetail;
    }
}
