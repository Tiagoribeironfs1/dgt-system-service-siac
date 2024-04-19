package com.siac.controller;

import java.util.Map;

public class ApiResponse {
    private boolean ok;
    private Map<String, Object> data;

    public ApiResponse(boolean ok, Map<String, Object> data) {
        this.ok = ok;
        this.data = data;
    }

    // Getters and Setters
    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
