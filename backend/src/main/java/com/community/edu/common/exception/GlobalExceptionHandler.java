package com.community.edu.common.exception;

import com.community.edu.common.response.ApiResponse;

/**
 * 全局异常处理器。统一处理Controller层抛出的异常。
 */
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> handleBizException(BizException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("business exception: code={}, message={}", errorCode.getCode(), ex.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatus())
            .body(ApiResponse.fail(errorCode.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return validationResponse(message);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return validationResponse(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.joining("; "));
        return validationResponse(message);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateKey(DuplicateKeyException ex) {
        log.warn("duplicate key", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.fail(ErrorCode.CONFLICT.getCode(), "数据已存在，请检查唯一字段"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("unexpected server error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage()));
    }

    private ResponseEntity<ApiResponse<Void>> validationResponse(String message) {
        String safeMessage = message == null || message.isBlank() ? ErrorCode.VALIDATION_FAILED.getMessage() : message;
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
            .body(ApiResponse.fail(ErrorCode.VALIDATION_FAILED.getCode(), safeMessage));
    }
}
