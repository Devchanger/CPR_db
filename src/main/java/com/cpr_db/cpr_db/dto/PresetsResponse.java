package com.cpr_db.cpr_db.dto;

import java.util.List;

public class PresetsResponse {

    private List<String> presets;

    public PresetsResponse() {
    }

    public PresetsResponse(List<String> presets) {
        this.presets = presets;
    }

    public List<String> getPresets() {
        return presets;
    }

    public void setPresets(List<String> presets) {
        this.presets = presets;
    }
}
