package com.hxcoe.common.exception;

import com.hxcoe.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        String errorMessage = "参数校验失败: " + String.join(", ", errors);
        logger.warn("请求参数校验失败 errors={}", errorMessage);
        return Result.badRequest(errorMessage);
    }

    @ExceptionHandler(BindException.class)
    public Result<Object> handleBindException(BindException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        String normalized = "参数绑定失败: " + errorMessage;
        logger.warn("请求参数绑定失败 errors={}", normalized);
        return Result.badRequest(normalized);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        String errorMessage = "参数校验失败: " + ex.getMessage();
        logger.warn("约束校验失败 error={}", errorMessage);
        return Result.badRequest(errorMessage);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.warn("请求体解析失败 error={}", ex.getMessage());
        return Result.badRequest("请求体解析失败");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("参数错误 error={}", ex.getMessage());
        return Result.badRequest(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Object> handleOtherExceptions(Exception ex) {
        logger.error("系统异常 error={}", ex.getMessage(), ex);
        return Result.systemError("系统错误: " + ex.getMessage());
    }
}
