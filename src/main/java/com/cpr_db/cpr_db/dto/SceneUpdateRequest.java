package com.cpr_db.cpr_db.dto;

import jakarta.validation.constraints.Min;

public class SceneUpdateRequest {
    private String name;
    private String description;
    private String type;
    private String icon;
    @Min(value = 0, message = "sortOrder must be >= 0")
    private Integer sortOrder;
    private String status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
