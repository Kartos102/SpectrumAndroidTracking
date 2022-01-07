package com.jo.spectrumtracking.model;

public class ShippingAddressHolder {
    private String name;
    private String email;
    private String streetAddress = "";
    private String city = "";
    private String zipCode;
    private String state;

    public ShippingAddressHolder(String name, String email, String streetAddress, String city, String zipCode, String state) {
        this.name = name;
        this.email = email;
        this.streetAddress = streetAddress;
        this.city = city;
        this.zipCode = zipCode;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
