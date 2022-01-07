package com.jo.spectrumtracking.model;

import java.util.Date;

public class Resp_TripLog {
    private int fatigueDriving;
    private double fuel;
    private int harshAcce;
    private int harshDece;
    private int idle;
    private double maxSpeed;
    private double mileage;
    private int speeding;
    private int stops;
    private Date dateTime;

    public Resp_TripLog(int fatigueDriving, double fuel, int harshAcce, int harshDece, int idle, double maxSpeed, double mileage, int speeding, int stops, Date dateTime) {
        this.fatigueDriving = fatigueDriving;
        this.fuel = fuel;
        this.harshAcce = harshAcce;
        this.harshDece = harshDece;
        this.idle = idle;
        this.maxSpeed = maxSpeed;
        this.mileage = mileage;
        this.speeding = speeding;
        this.stops = stops;
        this.dateTime = dateTime;
    }

    public int getFatigueDriving() {
        return fatigueDriving;
    }

    public void setFatigueDriving(int fatigueDriving) {
        this.fatigueDriving = fatigueDriving;
    }

    public double getFuel() {
        return fuel;
    }

    public void setFuel(double fuel) {
        this.fuel = fuel;
    }

    public int getHarshAcce() {
        return harshAcce;
    }

    public void setHarshAcce(int harshAcce) {
        this.harshAcce = harshAcce;
    }

    public int getHarshDece() {
        return harshDece;
    }

    public void setHarshDece(int harshDece) {
        this.harshDece = harshDece;
    }

    public int getIdle() {
        return idle;
    }

    public void setIdle(int idle) {
        this.idle = idle;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public double getMileage() {
        return mileage;
    }

    public void setMileage(double mileage) {
        this.mileage = mileage;
    }

    public int getSpeeding() {
        return speeding;
    }

    public void setSpeeding(int speeding) {
        this.speeding = speeding;
    }

    public int getStops() {
        return stops;
    }

    public void setStops(int stops) {
        this.stops = stops;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }
}
