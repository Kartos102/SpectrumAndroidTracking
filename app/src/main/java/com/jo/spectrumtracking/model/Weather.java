package com.jo.spectrumtracking.model;

import java.util.List;

public class Weather {
    private long dt;
    private Weather_Main main;
    private List<Weather_General> weather;
    private Weather_Cloud clouds;
    private Weather_Wind wind;
    private Weather_Rain rain;
    private Weather_sys sys;
    private String dt_txt;

    public Weather(long dt, Weather_Main main, List<Weather_General> weather, Weather_Cloud clouds, Weather_Wind wind, Weather_Rain rain, Weather_sys sys, String dt_txt) {
        this.dt = dt;
        this.main = main;
        this.weather = weather;
        this.clouds = clouds;
        this.wind = wind;
        this.rain = rain;
        this.sys = sys;
        this.dt_txt = dt_txt;
    }

    public class Weather_General {
        public long id;
        public String rain;
        public String description;
        public String icon;
    }

    public class Weather_Cloud {
        public int all;
    }

    public long getDt() {
        return dt;
    }

    public void setDt(long dt) {
        this.dt = dt;
    }

    public Weather_Main getMain() {
        return main;
    }

    public void setMain(Weather_Main main) {
        this.main = main;
    }

    public List<Weather_General> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather_General> weather) {
        this.weather = weather;
    }

    public Weather_Cloud getClouds() {
        return clouds;
    }

    public void setClouds(Weather_Cloud clouds) {
        this.clouds = clouds;
    }

    public Weather_Wind getWind() {
        return wind;
    }

    public void setWind(Weather_Wind wind) {
        this.wind = wind;
    }

    public Weather_Rain getRain() {
        return rain;
    }

    public void setRain(Weather_Rain rain) {
        this.rain = rain;
    }

    public Weather_sys getSys() {
        return sys;
    }

    public void setSys(Weather_sys sys) {
        this.sys = sys;
    }

    public String getDt_txt() {
        return dt_txt;
    }

    public void setDt_txt(String dt_txt) {
        this.dt_txt = dt_txt;
    }

    public class Weather_Main {
        public float temp;
        public float temp_min;
        public float temp_max;
        public float pressure;
        public float sea_level;
        public float grnd_level;
        public float humidity;
        public float temp_kf;
    }

    public class Weather_Wind {
        public float speed;
        public float deg;
    }

    public class Weather_Rain {
        public double rain;
    }

    public class Weather_sys {
        public String pod;
    }
}
