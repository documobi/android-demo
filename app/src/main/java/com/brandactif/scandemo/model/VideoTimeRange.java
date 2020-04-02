package com.brandactif.scandemo.model;

import com.google.gson.annotations.SerializedName;

public class VideoTimeRange {
    @SerializedName("start_at")
    private float startAt;
    @SerializedName("end_at")
    private float endAt;

    public VideoTimeRange(float startAt, float endAt) {
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public float getStartAt() {
        return startAt;
    }

    public void setStartAt(float startAt) {
        this.startAt = startAt;
    }

    public float getEndAt() {
        return endAt;
    }

    public void setEndAt(float endAt) {
        this.endAt = endAt;
    }

}
