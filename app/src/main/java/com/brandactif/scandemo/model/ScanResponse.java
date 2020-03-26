package com.brandactif.scandemo.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ScanResponse {

    @SerializedName("uuid")
    private String uuid;
    @SerializedName("status")
    private String status;
    @SerializedName("redirect_url")
    private String redirectUrl;
    @SerializedName("fallback_url")
    private String fallbackUrl;
    @SerializedName("details")
    private Map<String, String> details;

    public ScanResponse(String uuid, String status, String redirectUrl, String fallbackUrl, Map<String, String> details) {
        this.uuid = uuid;
        this.status = status;
        this.redirectUrl = redirectUrl;
        this.fallbackUrl = fallbackUrl;
        this.details = details;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getFallbackUrl() {
        return fallbackUrl;
    }

    public void setFallbackUrl(String fallbackUrl) {
        this.fallbackUrl = fallbackUrl;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("uuid = ").append(uuid)
                .append("\nstatus = ").append(status)
                .append("\nredirectUrl = ").append(redirectUrl)
                .append("\nfallbackUrl = ").append(fallbackUrl)
                .append("\ndetails = ").append(details).toString();
    }

}