package com.brandactif.scandemo.model;

import com.google.gson.annotations.SerializedName;

public class VideoScanDetail {

    @SerializedName("video_uuid")
    private String videoUuid;
    @SerializedName("time")
    private double time;
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;
    @SerializedName("metadata")
    private MetaData metaData;

    public VideoScanDetail(String videoUuid, double time, double latitude, double longitude, MetaData metaData) {
        this.videoUuid = videoUuid;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.metaData = metaData;
    }

    public String getVideoUuid() {
        return videoUuid;
    }

    public void setVideoUuid(String videoUuid) {
        this.videoUuid = videoUuid;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
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
