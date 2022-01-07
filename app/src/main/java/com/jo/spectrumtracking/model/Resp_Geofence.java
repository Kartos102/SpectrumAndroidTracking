package com.jo.spectrumtracking.model;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by JO on 3/21/2018.
 */

public class Resp_Geofence implements  Cloneable{
    private String id;
    private String name;
    private String type;
    private double lat;
    private double lng;
    private double radius;
    private ArrayList<ArrayList<Double>> boundary;
    private String note;

    public Resp_Geofence clone(){
        try {
            return  (Resp_Geofence)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Resp_Geofence(String id, String name, String type, double lat, double lng, double radius, ArrayList<ArrayList<Double>> bounday) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.boundary = bounday;
    }

    public Resp_Geofence() {
        this.id = String.format("%d", new Date().getTime());
        this.name = "";
        this.type = "Circle";
        this.lat = 0.0;
        this.lng = 0.0;
        this.radius = 300;
        this.boundary = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getRadius() {
        return radius;
    }

    public ArrayList<ArrayList<Double>> getBounday() {
        return boundary;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setBounday(ArrayList<ArrayList<Double>> bounday) {
        this.boundary = bounday;
    }
}
