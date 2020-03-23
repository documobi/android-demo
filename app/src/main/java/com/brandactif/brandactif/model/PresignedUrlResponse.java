package com.brandactif.brandactif.model;

import com.google.gson.annotations.SerializedName;

public class PresignedUrlResponse {

    @SerializedName("uuid")
    private String uuid;
    @SerializedName("upload_url")
    private String uploadUrl;

    public PresignedUrlResponse(String uuid, String uploadUrl) {
        this.uuid = uuid;
        this.uploadUrl = uploadUrl;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

}