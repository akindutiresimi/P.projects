package com.dtos.responses;

public class ApiResponse {
    private final boolean success;
    private final Object data;

    public ApiResponse(boolean success, Object data) {
        this.success = success;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getData() {
        return data;
    }
}
