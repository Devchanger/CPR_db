package com.cpr_db.cpr_db.dto;

import java.time.LocalDateTime;

public class StudentUpdateRequest {
    private String name;
    private String phone;
    private String email;
    private String groupName;
    private String certStatus;
    private LocalDateTime trainedAt;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getCertStatus() { return certStatus; }
    public void setCertStatus(String certStatus) { this.certStatus = certStatus; }
    public LocalDateTime getTrainedAt() { return trainedAt; }
    public void setTrainedAt(LocalDateTime trainedAt) { this.trainedAt = trainedAt; }
}
