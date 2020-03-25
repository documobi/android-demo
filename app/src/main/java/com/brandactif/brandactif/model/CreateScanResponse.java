package com.brandactif.brandactif.model;

import com.google.gson.annotations.SerializedName;

public class CreateScanResponse {

    @SerializedName("uuid")
    private String uuid;

    public CreateScanResponse(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


}
