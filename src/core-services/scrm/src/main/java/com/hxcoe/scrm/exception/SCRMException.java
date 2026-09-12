package com.hxcoe.scrm.exception;

import lombok.Getter;

/**
 * SCRM系统基础异常类
 */
@Getter
public class SCRMException extends RuntimeException {
    private final String errorCode;
    private final int statusCode;

    public SCRMException(String message, String errorCode, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public SCRMException(String message, String errorCode, int statusCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
}
