package com.hxcoe.common.result;

/**
 * 响应码枚举类
 */
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(0, "成功"),

    /**
     * 失败
     */
    FAIL(1, "失败"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未登录
     */
    NOT_LOGIN(401, "未登录"),

    /**
     * 无权限
     */
    NO_PERMISSION(403, "无权限"),

    /**
     * 数据不存在
     */
    DATA_NOT_EXIST(404, "数据不存在"),

    /**
     * 数据已存在
     */
    DATA_EXIST(409, "数据已存在"),

    /**
     * 系统错误
     */
    SYSTEM_ERROR(500, "系统错误"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务不可用");

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据响应码获取响应码枚举
     *
     * @param code 响应码
     * @return 响应码枚举
     */
    public static ResultCode getByCode(Integer code) {
        for (ResultCode resultCode : values()) {
            if (resultCode.getCode().equals(code)) {
                return resultCode;
            }
        }
        return null;
    }
}
