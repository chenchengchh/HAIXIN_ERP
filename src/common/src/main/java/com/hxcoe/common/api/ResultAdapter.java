package com.hxcoe.common.api;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.ResultCode;

/**
 * 将历史 Result 结构转换为 ApiResponse（对齐 code/msg/data）。
 */
public class ResultAdapter {

    private ResultAdapter() {
    }

    public static <T> ApiResponse<T> fromResult(Result<T> result) {
        if (result == null) {
            return ApiResponse.error(500, "系统错误");
        }
        Integer code = result.getCode();
        String message = result.getMessage();
        T data = result.getData();

        if (code != null && (code == 0 || code == 200)) {
            return ApiResponse.success(message, data);
        }

        return ApiResponse.error(mapToHttpCode(code), message);
    }

    private static int mapToHttpCode(Integer legacyCode) {
        if (legacyCode == null) return 500;
        if (legacyCode >= 100 && legacyCode <= 599) return legacyCode;

        if (legacyCode.equals(ResultCode.PARAM_ERROR.getCode())) return 400;
        if (legacyCode.equals(ResultCode.NOT_LOGIN.getCode())) return 401;
        if (legacyCode.equals(ResultCode.NO_PERMISSION.getCode())) return 403;
        if (legacyCode.equals(ResultCode.DATA_NOT_EXIST.getCode())) return 404;
        if (legacyCode.equals(ResultCode.SYSTEM_ERROR.getCode())) return 500;
        if (legacyCode.equals(ResultCode.SERVICE_UNAVAILABLE.getCode())) return 503;

        return 400;
    }
}

