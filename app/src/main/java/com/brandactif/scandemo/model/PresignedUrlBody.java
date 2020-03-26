package com.brandactif.scandemo.model;

import com.google.gson.annotations.SerializedName;

public class PresignedUrlBody {

    @SerializedName("type")
    private String type;
    @SerializedName("filename")
    private String fileName;

    public PresignedUrlBody(String type, String fileName) {
        this.type = type;
        this.fileName = fileName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}