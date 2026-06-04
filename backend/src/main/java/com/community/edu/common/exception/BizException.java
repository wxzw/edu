package com.community.edu.common.exception;

import lombok.Getter;

/**
 * 业务异常。用于抛出业务逻辑层面的错误。
 */

@Getter
public class BizException extends RuntimeException {

    private final ErrorCode errorCode;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
