package com.brandactif.scandemo.model;

import com.google.gson.annotations.SerializedName;

public class RadioScanDetail {

    @SerializedName("radio_name")
    private String radioName;
    @SerializedName("time")
    private String time;
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;
    @SerializedName("metadata")
    private MetaData metaData;

    public RadioScanDetail(String radioName, String time, double latitude, double longitude, MetaData metaData) {
        this.radioName = radioName;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.metaData = metaData;
    }

    public String getRadioName() {
        return radioName;
    }

    public void setRadioName(String radioName) {
        this.radioName = radioName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

}
