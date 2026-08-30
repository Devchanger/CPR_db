package com.cpr_db.cpr_db.dto;

public class VideoResponse {

    private String videoId;
    private String url;
    private Integer durationSeconds;
    private Long fileSize;

    public VideoResponse() {
    }

    public VideoResponse(String videoId, String url, Integer durationSeconds, Long fileSize) {
        this.videoId = videoId;
        this.url = url;
        this.durationSeconds = durationSeconds;
        this.fileSize = fileSize;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}
