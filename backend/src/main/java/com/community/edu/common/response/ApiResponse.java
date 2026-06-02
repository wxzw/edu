package com.community.edu.common.response;

import com.community.edu.common.trace.TraceIdHolder;
import java.time.OffsetDateTime;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final String code;
    private final String message;
    private final T data;
    private final String traceId;
    private final OffsetDateTime timestamp;

    private ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = TraceIdHolder.getTraceId();
        this.timestamp = OffsetDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "操作成功", data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>("SUCCESS", "操作成功", null);
    }

    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
