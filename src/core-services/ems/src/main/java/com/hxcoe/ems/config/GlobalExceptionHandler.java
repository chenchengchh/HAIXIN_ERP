package com.hxcoe.ems.config;

import com.hxcoe.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.StringJoiner;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringJoiner errorMsg = new StringJoiner(", ");
        for (FieldError fieldError : fieldErrors) {
            errorMsg.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        logger.warn("EMS参数校验失败 error={}", errorMsg);
        return Result.badRequest("参数校验失败: " + errorMsg);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringJoiner errorMsg = new StringJoiner(", ");
        for (FieldError fieldError : fieldErrors) {
            errorMsg.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        logger.warn("EMS参数绑定失败 error={}", errorMsg);
        return Result.badRequest("参数绑定失败: " + errorMsg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        logger.warn("EMS约束校验失败 error={}", e.getMessage());
        return Result.badRequest("参数校验失败: " + e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        logger.warn("EMS请求体解析失败 error={}", e.getMessage());
        return Result.badRequest("请求体解析失败");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        logger.warn("EMS参数错误 error={}", e.getMessage());
        return Result.badRequest(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        logger.error("EMS系统异常: {}", e.getMessage(), e);
        return Result.systemError("系统错误: " + e.getMessage());
    }
}

