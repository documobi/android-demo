package com.brandactif.brandactif.model;

import com.google.gson.annotations.SerializedName;

public class PresignedUrl {

    @SerializedName("presigned_url")
    private PresignedUrlBody presignedUrl;

    public PresignedUrl(String type, String fileName) {
        this.presignedUrl = new PresignedUrlBody(type, fileName);
    }

    public PresignedUrlBody getPresignedUrl() {
        return presignedUrl;
    }

    public void setPresignedUrl(PresignedUrlBody type) {
        this.presignedUrl = presignedUrl;
    }

}
