package com.brandactif.brandactif.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class MediaScanResponse {

    @SerializedName("redirect_url")
    private String redirectUrl;
    @SerializedName("response_details")
    private Map<String, String> responseDetails;

    public MediaScanResponse(String redirectUrl, Map<String, String> responseDetails) {
        this.redirectUrl = redirectUrl;
        this.responseDetails = responseDetails;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public Map<String, String> getResponseDetails() {
        return responseDetails;
    }

    public void setResponseDetails(Map<String, String> responseDetails) {
        this.responseDetails = responseDetails;
    }

}