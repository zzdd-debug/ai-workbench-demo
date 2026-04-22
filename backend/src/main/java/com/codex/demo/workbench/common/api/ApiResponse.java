package com.codex.demo.workbench.common.api;

import java.util.UUID;

public record ApiResponse<T>(
    boolean success,
    String traceId,
    T data,
    ApiError error
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, newTraceId(), data, null);
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(false, newTraceId(), null, new ApiError(code, message));
    }

    private static String newTraceId() {
        return "trace-" + UUID.randomUUID();
    }
}
