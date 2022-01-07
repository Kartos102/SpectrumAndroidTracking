package com.jo.spectrumtracking.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by JO on 3/21/2018.
 */

public class Resp_Tracker {
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
    @SerializedName("hotspot")
    private int hotspot;
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
    private double speedInMph;
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
    private double voltage;
    @SerializedName("tankVolume")//fuel
    private double tankVolume;
    @SerializedName("lastACCOnLat")//fuel
    private double lastACCOnLat;
    @SerializedName("lastACCOnLng")//fuel
    private double lastACCOnLng;
    @SerializedName("lastACCOffLat")//fuel
    private double lastACCOffLat;
    @SerializedName("lastACCOffLng")//fuel
    private double lastACCOffLng;
    @SerializedName("ACCStatus")//fuel
    private int ACCStatus;
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
    @SerializedName("geofence")
    private ArrayList<Resp_Geofence> geofence;

    @SerializedName("name")
    private String name;
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
    @SerializedName("driverPhoneNumber")
    private String driverPhoneNumber;
    @SerializedName("assetId")
    private String assetId;
    @SerializedName("heading")
    private double heading;
    @SerializedName("photoStatus")
    private boolean photoStatus;
    @SerializedName("coolantTemp")
    private double coolantTemp;
    @SerializedName("dataLimit")
    private float dataLimit;
    @SerializedName("dataVolumeCustomerCycle")
    private float dataVolumeCustomerCycle;
    @SerializedName("RPM")
    private float RPM;

    private String ACCOnAddress = "";
    private String ACCOffAddress = "";
    private boolean isSelected = false;
    private boolean changeFlag = false;
    private boolean isPhotoUpload = false;

    public Resp_Tracker(String _id, String spectrumId, String reportingId, String SIMCardNum, int __v, int hotspot, Date lastLogDateTime, double lat, Date latLngDateTime, Date expirationDate, String dataPlan, String LTEData, double lng, Double speedInMph, ArrayList<String> commandQueue, double lat1, double lat2, double lng1, double lng2, int harshAcce, int harshDece, int daySpeeding, Date lastACCOffTime, Date lastACCOnTime, Double voltage, Double tankVolume, Double lastACCOnLat, Double lastACCOnLng, Double lastACCOffLat, Double lastACCOffLng, Integer ACCStatus, String trackerModel, String country, float weekMile, float dayMile, float monthMile, float yearMile, String ACCOnAddress, String ACCOffAddress, String lastAlert, String color, String autoRenew, String plateNumber, String driverName, String assetId, Double heading, boolean photoStatus, Double coolantTemp) {
        this._id = _id;
        this.spectrumId = spectrumId;
        this.reportingId = reportingId;
        this.SIMCardNum = SIMCardNum;
        this.__v = __v;
        this.hotspot = hotspot;
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
        this.plateNumber = plateNumber;
        this.driverName = driverName;
        this.assetId = assetId;
        this.heading = heading;
        this.photoStatus = photoStatus;
        this.coolantTemp = coolantTemp;
    }
    public double getCoolantTemp(){
        return coolantTemp;
    }
    public String get_id() {
        if (_id == null) return "";
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getSpectrumId() {
        if (spectrumId == null) return "";
        return spectrumId;
    }

    public void setSpectrumId(String spectrumId) {
        this.spectrumId = spectrumId;
    }

    public String getReportingId() {
        if (reportingId == null) return "";
        return reportingId;
    }

    public void setReportingId(String reportingId) {
        this.reportingId = reportingId;
    }

    public String getSIMCardNum() {
        if (SIMCardNum == null) return "";
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

    public int getHotspot() {
        return hotspot;
    }

    public void setHotspot(int hotspot) {
        this.hotspot = hotspot;
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
        if (dataPlan == null) return "";
        return dataPlan;
    }

    public void setDataPlan(String dataPlan) {
        this.dataPlan = dataPlan;
    }

    public String getLTEData() {
        if (LTEData == null) return "";
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

    public double getSpeedInMph() {
        return speedInMph;
    }

    public void setSpeedInMph(double speedInMph) {
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

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public double getTankVolume() {
        return tankVolume;
    }

    public void setTankVolume(double tankVolume) {
        this.tankVolume = tankVolume;
    }

    public double getLastACCOnLat() {
        return lastACCOnLat;
    }

    public void setLastACCOnLat(double lastACCOnLat) {
        this.lastACCOnLat = lastACCOnLat;
    }

    public double getLastACCOnLng() {
        return lastACCOnLng;
    }

    public void setLastACCOnLng(double lastACCOnLng) {
        this.lastACCOnLng = lastACCOnLng;
    }

    public double getLastACCOffLat() {
        return lastACCOffLat;
    }

    public void setLastACCOffLat(double lastACCOffLat) {
        this.lastACCOffLat = lastACCOffLat;
    }

    public double getLastACCOffLng() {
        return lastACCOffLng;
    }

    public void setLastACCOffLng(double lastACCOffLng) {
        this.lastACCOffLng = lastACCOffLng;
    }

    public int getACCStatus() {
        return ACCStatus;
    }

    public void setACCStatus(int ACCStatus) {
        this.ACCStatus = ACCStatus;
    }

    public String getTrackerModel() {
        if (TrackerModel == null) return "";
        return TrackerModel;
    }

    public void setTrackerModel(String trackerModel) {
        TrackerModel = trackerModel;
    }

    public String getCountry() {
        if (country == null) return "";
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
        if (ACCOnAddress == null) return "";
        return ACCOnAddress;
    }

    public void setACCOnAddress(String ACCOnAddress) {
        this.ACCOnAddress = ACCOnAddress;
    }

    public String getACCOffAddress() {
        if (ACCOffAddress == null) return "";
        return ACCOffAddress;
    }

    public void setACCOffAddress(String ACCOffAddress) {
        this.ACCOffAddress = ACCOffAddress;
    }

    public String getLastAlert() {
        if (lastAlert == null) return "";
        return lastAlert;
    }

    public void setLastAlert(String lastAlert) {
        this.lastAlert = lastAlert;
    }

    public String getColor() {
        if (color == null) return "";
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAutoRenew() {
        if (autoRenew == null) return "";
        return autoRenew;
    }

    public void setAutoRenew(String autoRenew) {
        this.autoRenew = autoRenew;
    }

    public String getPlateNumber() {
        if (plateNumber == null) return "";
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getDriverName() {
        if (driverName == null) return "";
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getAssetId() {
        if (assetId == null) return "";
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public boolean isPhotoStatus() {
        return photoStatus;
    }

    public void setPhotoStatus(boolean photoStatus) {
        this.photoStatus = photoStatus;
    }

    public ArrayList<Resp_Geofence> getGeofence() {
        if (geofence == null) return new ArrayList<>();
        return geofence;
    }

    public void setGeofence(ArrayList<Resp_Geofence> geofence) {
        this.geofence = geofence;
    }

    public boolean getSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean getChangeFlag() {
        return changeFlag;
    }

    public void setChangeFlag(boolean changeFlag) {
        this.changeFlag = changeFlag;
    }

    public boolean getPhotoUpload() {
        return isPhotoUpload;
    }

    public void setPhotoUpload(boolean photoUpload) {
        isPhotoUpload = photoUpload;
    }

    public String getUserId() {
        if (userId == null) return "";
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        if (name == null) return "";
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriverPhoneNumber() {
        if (driverPhoneNumber == null) return "";
        return driverPhoneNumber;
    }

    public void setDriverPhoneNumber(String driverPhoneNumber) {
        this.driverPhoneNumber = driverPhoneNumber;
    }

    public float getDataLimit() {
        return dataLimit;
    }

    public void setDataLimit(float dataLimit) {
        this.dataLimit = dataLimit;
    }

    public float getDataVolumeCustomerCycle() {
        return dataVolumeCustomerCycle;
    }

    public void setDataVolumeCustomerCycle(float dataVolumeCustomerCycle) {
        this.dataVolumeCustomerCycle = dataVolumeCustomerCycle;
    }

    public float getRPM() {
        return RPM;
    }

    public void setRPM(float RPM) {
        this.RPM = RPM;
    }
}
