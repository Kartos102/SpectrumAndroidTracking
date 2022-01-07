package com.jo.spectrumtracking.model;

/**
 * Created by JO on 3/17/2018.
 */

public class OrderTracker {
    private String image;
    private String name;
    private String description;
    private Integer count;
    private Double price;

    public OrderTracker(String image, String name, String description, Integer count, Double price) {
        this.image = image;
        this.name = name;
        this.description = description;
        this.count = count;
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
