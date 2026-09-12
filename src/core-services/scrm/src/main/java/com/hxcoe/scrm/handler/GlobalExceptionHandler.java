package com.hxcoe.scrm.handler;

import com.hxcoe.common.result.Result;
import com.hxcoe.scrm.exception.SCRMException;
import com.hxcoe.scrm.exception.douyin.DouyinException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理抖音相关异常
     */
    @ExceptionHandler(DouyinException.class)
    public ResponseEntity<Result<Void>> handleDouyinException(DouyinException e) {
        log.warn("抖音异常 errorCode={} status={} message={}", e.getErrorCode(), e.getStatusCode(), e.getMessage(), e);
        return ResponseEntity
                .status(e.getStatusCode())
                .body(toResult(e.getStatusCode(), e.getMessage()));
    }

    /**
     * 处理SCRM系统异常
     */
    @ExceptionHandler(SCRMException.class)
    public ResponseEntity<Result<Void>> handleSCRMException(SCRMException e) {
        log.warn("SCRM业务异常 errorCode={} status={} message={}", e.getErrorCode(), e.getStatusCode(), e.getMessage(), e);
        return ResponseEntity
                .status(e.getStatusCode())
                .body(toResult(e.getStatusCode(), e.getMessage()));
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.systemError("系统内部错误，请联系管理员"));
    }

    private Result<Void> toResult(int statusCode, String message) {
        if (statusCode == HttpStatus.BAD_REQUEST.value()) {
            return Result.badRequest(message);
        }
        if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
            return Result.unauthorized(message);
        }
        if (statusCode == HttpStatus.FORBIDDEN.value()) {
            return Result.forbidden(message);
        }
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            return Result.notFound(message);
        }
        if (statusCode == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            return Result.serviceUnavailable(message);
        }
        return Result.error(statusCode, message);
    }
}
