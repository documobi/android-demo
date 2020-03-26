package com.brandactif.scandemo.model;

import com.google.gson.annotations.SerializedName;

public class RadioScan {

    @SerializedName("radio_scan")
    private RadioScanDetail radioScan;

    public RadioScan(String radioUuid, String time, double latitude, double longitude, MetaData metaData) {
        this.radioScan = new RadioScanDetail(radioUuid, time, latitude, longitude, metaData);
    }

    RadioScanDetail getRadioScan() {
        return radioScan;
    }

    void setRadioScan(RadioScanDetail radioScan) {
        this.radioScan = radioScan;
    }
}
