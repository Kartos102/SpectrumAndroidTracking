package com.jo.spectrumtracking.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

public class Resp_Tracker_Monitor {
    @SerializedName("_id")
    private String _id;
    @SerializedName("spectrumId")
    private String spectrumId;
    @SerializedName("reportingId")
    private String reportingId;
    @SerializedName("SIMCardNum")
    private String SIMCardNum;
    @SerializedName("__v")
    private int __v;
    //    @SerializedName("userId")
    //    public String userId;
    @SerializedName("lastLogDateTime")
    private Date lastLogDateTime;
    @SerializedName("lat")
    private double lat;
    @SerializedName("latLngDateTime")
    private Date latLngDateTime;
    @SerializedName("expirationDate")
    private Date expirationDate;
    @SerializedName("dataPlan")
    private String dataPlan;
    @SerializedName("LTEData")
    private String LTEData;
    @SerializedName("lng")
    private double lng;
    @SerializedName("speedInMph")
    private Double speedInMph;
    @SerializedName("commandQueue")
    private ArrayList<String> commandQueue;
    @SerializedName("lat1")
    private double lat1;
    @SerializedName("lat2")
    private double lat2;
    @SerializedName("lng1")
    private double lng1;
    @SerializedName("lng2")
    private double lng2;
    @SerializedName("harshAcce")
    private int harshAcce;
    @SerializedName("harshDece")
    private int harshDece;
    @SerializedName("daySpeeding")
    private int daySpeeding;
    @SerializedName("lastACCOffTime")//voltage
    private Date lastACCOffTime;
    @SerializedName("lastACCOnTime")//voltage
    private Date lastACCOnTime;
    @SerializedName("voltage")//fuel
    private Double voltage;
    @SerializedName("battery")//fuel
    private Double battery;
    @SerializedName("tankVolume")//fuel
    private Double tankVolume;
    @SerializedName("lastACCOnLat")//fuel
    private Double lastACCOnLat;
    @SerializedName("lastACCOnLng")//fuel
    private Double lastACCOnLng;
    @SerializedName("lastACCOffLat")//fuel
    private Double lastACCOffLat;
    @SerializedName("lastACCOffLng")//fuel
    private Double lastACCOffLng;
    @SerializedName("ACCStatus")//fuel
    private Integer ACCStatus;
    @SerializedName("TrackerModel")
    private String TrackerModel;
    @SerializedName("country")
    private String country;
    @SerializedName("weekMile")
    private float weekMile;
    @SerializedName("dayMile")
    private float dayMile;
    @SerializedName("monthMile")
    private float monthMile;
    @SerializedName("yearMile")
    private float yearMile;

    private String ACCOnAddress;
    private String ACCOffAddress;

    @SerializedName("lastAlert")
    private String lastAlert;
    @SerializedName("color")
    private String color;
    @SerializedName("autoRenew")
    private String autoRenew;
    @SerializedName("userId")
    private String userId;
    @SerializedName("plateNumber")
    private String plateNumber;
    @SerializedName("driverName")
    private String driverName;
    @SerializedName("assetId")
    private String assetId;
    @SerializedName("heading")
    private Double heading;
    @SerializedName("photoStatus")
    private boolean photoStatus;

    public Resp_Tracker_Monitor(String _id, String spectrumId, String reportingId, String SIMCardNum, int __v, Date lastLogDateTime, double lat, Date latLngDateTime, Date expirationDate, String dataPlan, String LTEData, double lng, Double speedInMph, ArrayList<String> commandQueue, double lat1, double lat2, double lng1, double lng2, int harshAcce, int harshDece, int daySpeeding, Date lastACCOffTime, Date lastACCOnTime, Double voltage, Double tankVolume, Double lastACCOnLat, Double lastACCOnLng, Double lastACCOffLat, Double lastACCOffLng, Integer ACCStatus, String trackerModel, String country, float weekMile, float dayMile, float monthMile, float yearMile, String ACCOnAddress, String ACCOffAddress, String lastAlert, String color, String autoRenew, String userId, String plateNumber, String driverName, String assetId, Double heading, boolean photoStatus, Double battery) {
        this._id = _id;
        this.spectrumId = spectrumId;
        this.reportingId = reportingId;
        this.SIMCardNum = SIMCardNum;
        this.__v = __v;
        this.lastLogDateTime = lastLogDateTime;
        this.lat = lat;
        this.latLngDateTime = latLngDateTime;
        this.expirationDate = expirationDate;
        this.dataPlan = dataPlan;
        this.LTEData = LTEData;
        this.lng = lng;
        this.speedInMph = speedInMph;
        this.commandQueue = commandQueue;
        this.lat1 = lat1;
        this.lat2 = lat2;
        this.lng1 = lng1;
        this.lng2 = lng2;
        this.harshAcce = harshAcce;
        this.harshDece = harshDece;
        this.daySpeeding = daySpeeding;
        this.lastACCOffTime = lastACCOffTime;
        this.lastACCOnTime = lastACCOnTime;
        this.voltage = voltage;
        this.battery = battery;
        this.tankVolume = tankVolume;
        this.lastACCOnLat = lastACCOnLat;
        this.lastACCOnLng = lastACCOnLng;
        this.lastACCOffLat = lastACCOffLat;
        this.lastACCOffLng = lastACCOffLng;
        this.ACCStatus = ACCStatus;
        TrackerModel = trackerModel;
        this.country = country;
        this.weekMile = weekMile;
        this.dayMile = dayMile;
        this.monthMile = monthMile;
        this.yearMile = yearMile;
        this.ACCOnAddress = ACCOnAddress;
        this.ACCOffAddress = ACCOffAddress;
        this.lastAlert = lastAlert;
        this.color = color;
        this.autoRenew = autoRenew;
        this.userId = userId;
        this.plateNumber = plateNumber;
        this.driverName = driverName;
        this.assetId = assetId;
        this.heading = heading;
        this.photoStatus = photoStatus;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getSpectrumId() {
        return spectrumId;
    }

    public void setSpectrumId(String spectrumId) {
        this.spectrumId = spectrumId;
    }

    public String getReportingId() {
        return reportingId;
    }

    public void setReportingId(String reportingId) {
        this.reportingId = reportingId;
    }

    public String getSIMCardNum() {
        return SIMCardNum;
    }

    public void setSIMCardNum(String SIMCardNum) {
        this.SIMCardNum = SIMCardNum;
    }

    public int get__v() {
        return __v;
    }

    public void set__v(int __v) {
        this.__v = __v;
    }

    public Date getLastLogDateTime() {
        return lastLogDateTime;
    }

    public void setLastLogDateTime(Date lastLogDateTime) {
        this.lastLogDateTime = lastLogDateTime;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public Date getLatLngDateTime() {
        return latLngDateTime;
    }

    public void setLatLngDateTime(Date latLngDateTime) {
        this.latLngDateTime = latLngDateTime;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getDataPlan() {
        return dataPlan;
    }

    public void setDataPlan(String dataPlan) {
        this.dataPlan = dataPlan;
    }

    public String getLTEData() {
        return LTEData;
    }

    public void setLTEData(String LTEData) {
        this.LTEData = LTEData;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public Double getSpeedInMph() {
        return speedInMph;
    }

    public void setSpeedInMph(Double speedInMph) {
        this.speedInMph = speedInMph;
    }

    public ArrayList<String> getCommandQueue() {
        return commandQueue;
    }

    public void setCommandQueue(ArrayList<String> commandQueue) {
        this.commandQueue = commandQueue;
    }

    public double getLat1() {
        return lat1;
    }

    public void setLat1(double lat1) {
        this.lat1 = lat1;
    }

    public double getLat2() {
        return lat2;
    }

    public void setLat2(double lat2) {
        this.lat2 = lat2;
    }

    public double getLng1() {
        return lng1;
    }

    public void setLng1(double lng1) {
        this.lng1 = lng1;
    }

    public double getLng2() {
        return lng2;
    }

    public void setLng2(double lng2) {
        this.lng2 = lng2;
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

    public int getDaySpeeding() {
        return daySpeeding;
    }

    public void setDaySpeeding(int daySpeeding) {
        this.daySpeeding = daySpeeding;
    }

    public Date getLastACCOffTime() {
        return lastACCOffTime;
    }

    public void setLastACCOffTime(Date lastACCOffTime) {
        this.lastACCOffTime = lastACCOffTime;
    }

    public Date getLastACCOnTime() {
        return lastACCOnTime;
    }

    public void setLastACCOnTime(Date lastACCOnTime) {
        this.lastACCOnTime = lastACCOnTime;
    }

    public Double getVoltage() {
        if (voltage == null || voltage == 0.0) {
            if (battery == null) {
                return 0.0;
            } else {
                return battery;
            }
        }
        return voltage;
    }

    public void setVoltage(Double voltage) {
        this.voltage = voltage;
    }

    public Double getTankVolume() {
        return tankVolume;
    }

    public void setTankVolume(Double tankVolume) {
        this.tankVolume = tankVolume;
    }

    public Double getLastACCOnLat() {
        return lastACCOnLat;
    }

    public void setLastACCOnLat(Double lastACCOnLat) {
        this.lastACCOnLat = lastACCOnLat;
    }

    public Double getLastACCOnLng() {
        return lastACCOnLng;
    }

    public void setLastACCOnLng(Double lastACCOnLng) {
        this.lastACCOnLng = lastACCOnLng;
    }

    public Double getLastACCOffLat() {
        return lastACCOffLat;
    }

    public void setLastACCOffLat(Double lastACCOffLat) {
        this.lastACCOffLat = lastACCOffLat;
    }

    public Double getLastACCOffLng() {
        return lastACCOffLng;
    }

    public void setLastACCOffLng(Double lastACCOffLng) {
        this.lastACCOffLng = lastACCOffLng;
    }

    public Integer getACCStatus() {
        return ACCStatus;
    }

    public void setACCStatus(Integer ACCStatus) {
        this.ACCStatus = ACCStatus;
    }

    public String getTrackerModel() {
        return TrackerModel;
    }

    public void setTrackerModel(String trackerModel) {
        TrackerModel = trackerModel;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public float getWeekMile() {
        return weekMile;
    }

    public void setWeekMile(float weekMile) {
        this.weekMile = weekMile;
    }

    public float getDayMile() {
        return dayMile;
    }

    public void setDayMile(float dayMile) {
        this.dayMile = dayMile;
    }

    public float getMonthMile() {
        return monthMile;
    }

    public void setMonthMile(float monthMile) {
        this.monthMile = monthMile;
    }

    public float getYearMile() {
        return yearMile;
    }

    public void setYearMile(float yearMile) {
        this.yearMile = yearMile;
    }

    public String getACCOnAddress() {
        return ACCOnAddress;
    }

    public void setACCOnAddress(String ACCOnAddress) {
        this.ACCOnAddress = ACCOnAddress;
    }

    public String getACCOffAddress() {
        return ACCOffAddress;
    }

    public void setACCOffAddress(String ACCOffAddress) {
        this.ACCOffAddress = ACCOffAddress;
    }

    public String getLastAlert() {
        return lastAlert;
    }

    public void setLastAlert(String lastAlert) {
        this.lastAlert = lastAlert;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    public boolean isPhotoStatus() {
        return photoStatus;
    }

    public void setPhotoStatus(boolean photoStatus) {
        this.photoStatus = photoStatus;
    }
}
