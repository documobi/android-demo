package com.brandactif.brandactif.model;

import com.google.gson.annotations.SerializedName;

public class TvScanDetail {

    @SerializedName("tv_uuid")
    private String tvUuid;
    @SerializedName("time")
    private String time;
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;
    @SerializedName("metadata")
    private MetaData metaData;

    public TvScanDetail(String tvUuid, String time, double latitude, double longitude, MetaData metaData) {
        this.tvUuid = tvUuid;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.metaData = metaData;
    }

    public String getTvUuid() {
        return tvUuid;
    }

    public void setTvUuid(String radioUuid) {
        this.tvUuid = tvUuid;
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
