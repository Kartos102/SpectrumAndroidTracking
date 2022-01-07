package com.jo.spectrumtracking.model;

public class Resp_ImageUrl {
    private String url;
    private boolean success = false;

    public Resp_ImageUrl(String url, boolean success) {
        this.url = url;
        this.success = success;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
