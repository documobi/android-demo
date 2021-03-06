package com.brandactif.scandemo.model;

import com.google.gson.annotations.SerializedName;

public class Scan {

    @SerializedName("scan")
    private ScanDetail scan;

    public Scan(String imageFileName, String imageUuid, double latitude, double longitude, String metaData) {
        this.scan = new ScanDetail(imageFileName, imageUuid, latitude, longitude, metaData);
    }

    public ScanDetail getScan() {
        return scan;
    }

    public void setScan(ScanDetail scan) {
        this.scan = scan;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("fileName = ").append(scan.getImageFilename())
                .append("\nuuid = ").append(scan.getImageUuid())
                .append("\nlatitude = ").append(scan.getLatitude())
                .append("\nlongitude = ").append(scan.getLongitude())
                .append("\nmetaData = ").append(scan.getMetaData()).toString();
    }
}
