package com.jo.spectrumtracking.model;

/**
 * Created by JO on 3/18/2018.
 */

public class LTEData {
    private String lteData;
    private double price;

    public LTEData(String lteData, double price) {
        this.lteData = lteData;
        this.price = price;
    }

    public String getLteData() {
        return lteData;
    }

    public void setLteData(String lteData) {
        this.lteData = lteData;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
