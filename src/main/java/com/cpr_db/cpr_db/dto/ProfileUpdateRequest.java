package com.cpr_db.cpr_db.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public class ProfileUpdateRequest {

    private String realName;

    @Min(value = 0, message = "gender must be 0, 1 or 2")
    @Max(value = 2, message = "gender must be 0, 1 or 2")
    private Integer gender;

    @Pattern(regexp = "^(\\+86)?1[3-9]\\d{9}$|^$", message = "invalid phone number format")
    private String phone;

    private String studentId;

    private String className;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
}
