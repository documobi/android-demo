package com.brandactif.scandemo.model;

import com.google.gson.annotations.SerializedName;

public class ScanDetail {

    @SerializedName("image_filename")
    private String imageFilename;
    @SerializedName("image_uuid")
    private String imageUuid;
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;
    @SerializedName("metadata")
    private String metaData;

    public ScanDetail(String imageFileName, String imageUuid, double latitude, double longitude, String metaData) {
        this.imageFilename = imageFileName;
        this.imageUuid = imageUuid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.metaData = metaData;
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(String imageFileName) {
        this.imageFilename = imageFileName;
    }

    public String getImageUuid() {
        return imageUuid;
    }

    public void setImageUuid(String imageUuid) {
        this.imageUuid = imageUuid;
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

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

}



