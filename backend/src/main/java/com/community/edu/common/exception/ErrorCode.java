package com.community.edu.common.exception;

import lombok.Getter;

/**
 * 错误码枚举。定义系统中所有业务错误码。
 */
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    BAD_REQUEST("BAD_REQUEST", "请求参数不正确", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("UNAUTHORIZED", "请先登录", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "无权访问该资源", HttpStatus.FORBIDDEN),
    NOT_FOUND("NOT_FOUND", "资源不存在", HttpStatus.NOT_FOUND),
    CONFLICT("CONFLICT", "数据冲突", HttpStatus.CONFLICT),
    VALIDATION_FAILED("VALIDATION_FAILED", "参数校验失败", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("INTERNAL_ERROR", "系统繁忙，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR),
    BIZ_ERROR("INTERNAL_ERROR", "系统繁忙，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
