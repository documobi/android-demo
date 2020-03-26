package com.brandactif.scandemo.model;

import com.google.gson.annotations.SerializedName;

public class TvScan {

    @SerializedName("tv_scan")
    private TvScanDetail tvScan;

    public TvScan(String tvUuid, String time, double latitude, double longitude, MetaData metaData) {
        this.tvScan = new TvScanDetail(tvUuid, time, latitude, longitude, metaData);
    }

    TvScanDetail getTvScan() {
        return tvScan;
    }

    void setTvScan(TvScanDetail tvScan) {
        this.tvScan = tvScan;
    }

}
