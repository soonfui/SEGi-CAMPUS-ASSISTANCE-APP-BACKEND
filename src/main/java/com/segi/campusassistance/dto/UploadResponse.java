package com.segi.campusassistance.dto;

public class UploadResponse {

    private String url;

    public UploadResponse() {
    }

    public UploadResponse(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

