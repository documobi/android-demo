package com.brandactif.brandactif.model;

import com.google.gson.annotations.SerializedName;

public class VideoScan {

    @SerializedName("video_scan")
    private VideoScanDetail videoScan;

    public VideoScan(String videoUuid, double time, double latitude, double longitude, MetaData metaData) {
        this.videoScan = new VideoScanDetail(videoUuid, time, latitude, longitude, metaData);
    }

    VideoScanDetail getVideoScan() {
        return videoScan;
    }

    void setVideoScan(VideoScanDetail videoScan) {
        this.videoScan = videoScan;
    }

}
