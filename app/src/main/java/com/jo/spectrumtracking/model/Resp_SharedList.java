package com.jo.spectrumtracking.model;

public class Resp_SharedList {
    private String email;
    private Boolean checked;

    public Resp_SharedList() {
    }

    public Resp_SharedList(String email, Boolean checked) {
        this.email = email;
        this.checked = checked;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }
}
