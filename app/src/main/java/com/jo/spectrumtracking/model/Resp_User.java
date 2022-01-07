package com.jo.spectrumtracking.model;

/**
 * Created by JO on 3/21/2018.
 */

public class Resp_User {
    private String email;
    private String firstName;
    private String lastName;
    private Billing billing;

    public static class Billing {
        public String address1;
        public String address2;
        public String city;
        public String state;
        public String zip;
        public String phone;
        public String creditCardToken;
        public String creditCardExpirationDate;
    }

    public Resp_User(String email, String firstName, String lastName, Billing billing) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.billing = billing;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Billing getBilling() {
        return billing;
    }

    public void setBilling(Billing billing) {
        this.billing = billing;
    }
}

