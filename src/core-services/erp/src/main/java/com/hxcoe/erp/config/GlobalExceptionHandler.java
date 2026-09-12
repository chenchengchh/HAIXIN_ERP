package com.hxcoe.erp.config;

import com.hxcoe.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.StringJoiner;

/**
 * ERP 全局异常处理器，统一返回标准 Result 契约。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理参数错误异常
     *
     * @param e 参数错误异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("ERP参数错误 error={}", e.getMessage());
        return Result.badRequest(e.getMessage());
    }

    /**
     * 处理运行时异常。
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        logger.error("ERP运行时异常 error={}", e.getMessage(), e);
        return Result.systemError("系统运行时错误: " + e.getMessage());
    }

    /**
     * 处理参数校验异常。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        logger.warn("ERP参数校验异常 error={}", e.getMessage());
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringJoiner errorMsg = new StringJoiner(", ");
        for (FieldError fieldError : fieldErrors) {
            errorMsg.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        return Result.badRequest("参数校验失败: " + errorMsg);
    }

    /**
     * 处理绑定异常。
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        logger.warn("ERP请求参数绑定异常 error={}", e.getMessage());
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringJoiner errorMsg = new StringJoiner(", ");
        for (FieldError fieldError : fieldErrors) {
            errorMsg.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        return Result.badRequest("请求参数绑定失败: " + errorMsg);
    }

    /**
     * 处理兜底异常。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        logger.error("ERP系统异常 error={}", e.getMessage(), e);
        return Result.systemError("系统错误: " + e.getMessage());
    }
}
