package com.brandactif.brandactif.model;

import com.google.gson.annotations.SerializedName;

public class MetaData {

    @SerializedName("device_id")
    private String deviceId;
    @SerializedName("platform")
    private String platform;
    @SerializedName("os_version")
    private String osVersion;
    @SerializedName("browser")
    private String browser;
    @SerializedName("language")
    private String language;
    @SerializedName("model")
    private String model;

    public MetaData(String deviceId, String platform, String osVersion, String browser, String language, String model) {
        this.deviceId = deviceId;
        this.platform = platform;
        this.osVersion = osVersion;
        this.browser = browser;
        this.language = language;
        this.model = model;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String languange) {
        this.language = languange;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
