package com.hxcoe.scrm.exception.douyin;

import com.hxcoe.scrm.exception.SCRMException;

/**
 * 抖音相关异常类
 */
public class DouyinException extends SCRMException {
    public DouyinException(String message) {
        super(message, "DOUYIN_ERROR", 400);
    }

    public DouyinException(String message, Throwable cause) {
        super(message, "DOUYIN_ERROR", 400, cause);
    }

    public DouyinException(String message, String errorCode, int statusCode) {
        super(message, errorCode, statusCode);
    }

    public DouyinException(String message, String errorCode, int statusCode, Throwable cause) {
        super(message, errorCode, statusCode, cause);
    }
}
