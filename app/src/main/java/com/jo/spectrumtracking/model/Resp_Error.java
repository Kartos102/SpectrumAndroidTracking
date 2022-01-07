package com.jo.spectrumtracking.model;

/**
 * Created by JO on 3/21/2018.
 */

public class Resp_Error {
    private String name;
    private String message;
    private int httpStatus;

    public Resp_Error(String name, String message, int httpStatus) {
        this.name = name;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }
}

