package com.jo.spectrumtracking.model;

public class Resp_Alarm {
    public String speedLimit;
    public String fatigueTime;
    public String harshTurn;
    public String harshAcceleration;
    public String harshDeceleration;
    public String email;
    public String phoneNumber;
    public Boolean  speedingAlarmStatus;
    public Boolean  fatigueAlarmStatus;
    public Boolean  harshTurnAlarmStatus;
    public Boolean  harshAcceAlarmStatus;
    public Boolean  harshDeceAlarmStatus;
    public Boolean  airplaneMode;
    public Boolean  tamperAlarmStatus;
    public Boolean  geoFenceAlarmStatus;
    public Boolean  emailAlarmStatus;
    public Boolean  phoneAlarmStatus;
    public Boolean accAlarmStatus;
    public Boolean soundAlarmStatus;
    public Boolean vibrationAlarmStatus;
    public Boolean stopAlarmStatus;
    public Boolean coolantTempAlarmStatus;
    public Boolean engineIdleAlarmStatus;
    public Boolean engineAlarmStatus;

    public Resp_Alarm(String speedLimit, String fatigueTime, String harshTurn, String harshAcceleration, String harshDeceleration, String email, String phoneNumber, Boolean speedingAlarmStatus, Boolean fatigueAlarmStatus, Boolean harshTurnAlarmStatus, Boolean harshAcceAlarmStatus, Boolean harshDeceAlarmStatus, Boolean airplaneMode, Boolean tamperAlarmStatus, Boolean geoFenceAlarmStatus, Boolean emailAlarmStatus, Boolean phoneAlarmStatus, Boolean accAlarmStatus, Boolean soundAlarmStatus, Boolean vibrationAlarmStatus, Boolean stopAlarmStatus, Boolean coolantTempAlarmStatus, Boolean engineIdleAlarmStatus, Boolean engineAlarmStatus) {
        this.speedLimit = speedLimit;
        this.fatigueTime = fatigueTime;
        this.harshTurn = harshTurn;
        this.harshAcceleration = harshAcceleration;
        this.harshDeceleration = harshDeceleration;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.speedingAlarmStatus = speedingAlarmStatus;
        this.fatigueAlarmStatus = fatigueAlarmStatus;
        this.harshTurnAlarmStatus = harshTurnAlarmStatus;
        this.harshAcceAlarmStatus = harshAcceAlarmStatus;
        this.harshDeceAlarmStatus = harshDeceAlarmStatus;
        this.airplaneMode = airplaneMode;
        this.tamperAlarmStatus = tamperAlarmStatus;
        this.geoFenceAlarmStatus = geoFenceAlarmStatus;
        this.emailAlarmStatus = emailAlarmStatus;
        this.phoneAlarmStatus = phoneAlarmStatus;
        this.accAlarmStatus = accAlarmStatus;
        this.soundAlarmStatus = soundAlarmStatus;
        this.vibrationAlarmStatus = vibrationAlarmStatus;
        this.stopAlarmStatus = stopAlarmStatus;
        this.coolantTempAlarmStatus = coolantTempAlarmStatus;
        this.engineIdleAlarmStatus = engineIdleAlarmStatus;
        this.engineAlarmStatus = engineAlarmStatus;
    }

    public String getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(String speedLimit) {
        this.speedLimit = speedLimit;
    }

    public String getFatigueTime() {
        return fatigueTime;
    }

    public void setFatigueTime(String fatigueTime) {
        this.fatigueTime = fatigueTime;
    }

    public String getHarshTurn() {
        return harshTurn;
    }

    public void setHarshTurn(String harshTurn) {
        this.harshTurn = harshTurn;
    }

    public String getHarshAcceleration() {
        return harshAcceleration;
    }

    public void setHarshAcceleration(String harshAcceleration) {
        this.harshAcceleration = harshAcceleration;
    }

    public String getHarshDeceleration() {
        return harshDeceleration;
    }

    public void setHarshDeceleration(String harshDeceleration) {
        this.harshDeceleration = harshDeceleration;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getSpeedingAlarmStatus() {
        return speedingAlarmStatus;
    }

    public void setSpeedingAlarmStatus(Boolean speedingAlarmStatus) {
        this.speedingAlarmStatus = speedingAlarmStatus;
    }

    public Boolean getFatigueAlarmStatus() {
        return fatigueAlarmStatus;
    }

    public void setFatigueAlarmStatus(Boolean fatigueAlarmStatus) {
        this.fatigueAlarmStatus = fatigueAlarmStatus;
    }

    public Boolean getHarshTurnAlarmStatus() {
        return harshTurnAlarmStatus;
    }

    public void setHarshTurnAlarmStatus(Boolean harshTurnAlarmStatus) {
        this.harshTurnAlarmStatus = harshTurnAlarmStatus;
    }

    public Boolean getHarshAcceAlarmStatus() {
        return harshAcceAlarmStatus;
    }

    public void setHarshAcceAlarmStatus(Boolean harshAcceAlarmStatus) {
        this.harshAcceAlarmStatus = harshAcceAlarmStatus;
    }

    public Boolean getHarshDeceAlarmStatus() {
        return harshDeceAlarmStatus;
    }

    public void setHarshDeceAlarmStatus(Boolean harshDeceAlarmStatus) {
        this.harshDeceAlarmStatus = harshDeceAlarmStatus;
    }

    public Boolean getAirplaneMode() {
        return airplaneMode;
    }

    public void setAirplaneMode(Boolean airplaneMode) {
        this.airplaneMode = airplaneMode;
    }

    public Boolean getTamperAlarmStatus() {
        return tamperAlarmStatus;
    }

    public void setTamperAlarmStatus(Boolean tamperAlarmStatus) {
        this.tamperAlarmStatus = tamperAlarmStatus;
    }

    public Boolean getGeoFenceAlarmStatus() {
        return geoFenceAlarmStatus;
    }

    public void setGeoFenceAlarmStatus(Boolean geoFenceAlarmStatus) {
        this.geoFenceAlarmStatus = geoFenceAlarmStatus;
    }

    public Boolean getEmailAlarmStatus() {
        return emailAlarmStatus;
    }

    public void setEmailAlarmStatus(Boolean emailAlarmStatus) {
        this.emailAlarmStatus = emailAlarmStatus;
    }

    public Boolean getPhoneAlarmStatus() {
        return phoneAlarmStatus;
    }

    public void setPhoneAlarmStatus(Boolean phoneAlarmStatus) {
        this.phoneAlarmStatus = phoneAlarmStatus;
    }

    public Boolean getAccAlarmStatus() {
        return accAlarmStatus;
    }

    public void setAccAlarmStatus(Boolean accAlarmStatus) {
        this.accAlarmStatus = accAlarmStatus;
    }

    public Boolean getSoundAlarmStatus() {
        return soundAlarmStatus;
    }

    public void setSoundAlarmStatus(Boolean soundAlarmStatus) {
        this.soundAlarmStatus = soundAlarmStatus;
    }

    public Boolean getVibrationAlarmStatus() {
        return vibrationAlarmStatus;
    }

    public void setVibrationAlarmStatus(Boolean vibrationAlarmStatus) {
        this.vibrationAlarmStatus = vibrationAlarmStatus;
    }

    public Boolean getStopAlarmStatus() {
        return stopAlarmStatus;
    }

    public void setStopAlarmStatus(Boolean stopAlarmStatus) {
        this.stopAlarmStatus = stopAlarmStatus;
    }
}
