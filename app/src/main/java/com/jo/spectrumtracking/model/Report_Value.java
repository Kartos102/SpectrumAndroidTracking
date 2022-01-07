package com.jo.spectrumtracking.model;

import java.util.Date;

public class Report_Value {
    private Date date;
    private String value;

    public Report_Value(Date date, String value) {
        this.date = date;
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
