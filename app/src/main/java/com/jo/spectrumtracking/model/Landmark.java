package com.jo.spectrumtracking.model;

/**
 * Created by JO on 3/18/2018.
 */

public class Landmark {
    public String name;
    public String lat;
    public String lng;
    public String type;

    public Landmark(String name, String type, String lat, String lng) {
        this.name = name;
        this.type = type;
        this.lat = lat;
        this.lng = lng;
    }
}
