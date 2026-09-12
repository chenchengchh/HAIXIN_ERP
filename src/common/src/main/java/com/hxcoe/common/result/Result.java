package com.hxcoe.common.result;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hxcoe.common.web.RequestContextSupport;
import java.io.Serializable;
import java.util.Date;
import org.springframework.util.StringUtils;

/**
 * 统一响应结果类
 *
 * @param <T> 响应数据类型
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码：0-成功，非0-失败
     */
    private Integer code;

    /**
     * 响应消息
     */
    @JsonProperty("msg")
    @JsonAlias("message")
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳
     */
    private Date timestamp;

    /**
     * 当前响应关联的链路追踪ID
     */
    @JsonProperty("traceId")
    @JsonAlias("trace_id")
    private String traceId;

    /**
     * 当前请求ID
     */
    @JsonProperty("requestId")
    @JsonAlias("request_id")
    private String requestId;

    /**
     * 当前服务名
     */
    @JsonProperty("service")
    private String service;

    public Result() {
    }

    public Result(Integer code, String message, T data, Date timestamp) {
        this(
                code,
                message,
                data,
                timestamp,
                RequestContextSupport.currentTraceId(),
                RequestContextSupport.currentRequestId(),
                RequestContextSupport.currentService()
        );
    }

    public Result(
            Integer code,
            String message,
            T data,
            Date timestamp,
            String traceId,
            String requestId,
            String service
    ) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
        this.traceId = normalize(traceId);
        this.requestId = normalize(requestId);
        this.service = normalize(service);
    }

    @JsonProperty("msg")
    public String getMsg() {
        return message;
    }

    @JsonProperty("msg")
    public void setMsg(String msg) {
        this.message = msg;
    }

    /**
     * 成功响应
     *
     * @param <T> 响应数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success() {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应
     *
     * @param data 响应数据
     * @param <T>  响应数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(T data) {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     响应数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(String message, T data) {
        return build(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 成功响应
     *
     * @param message 响应消息
     * @param <T>     响应数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(String message) {
        return build(ResultCode.SUCCESS.getCode(), message, null);
    }

    /**
     * 失败响应
     *
     * @param <T> 响应数据类型
     * @return 失败响应结果
     */
    public static <T> Result<T> fail() {
        return build(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMessage(), null);
    }

    /**
     * 失败响应
     *
     * @param message 响应消息
     * @param <T>     响应数据类型
     * @return 失败响应结果
     */
    public static <T> Result<T> fail(String message) {
        return build(ResultCode.FAIL.getCode(), message, null);
    }

    /**
     * 失败响应
     *
     * @param code    响应码
     * @param message 响应消息
     * @param <T>     响应数据类型
     * @return 失败响应结果
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return build(code, message, null);
    }

    /**
     * 失败响应
     *
     * @param resultCode 响应码枚举
     * @param <T>        响应数据类型
     * @return 失败响应结果
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return build(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 错误响应
     *
     * @param message 响应消息
     * @param <T>     响应数据类型
     * @return 错误响应结果
     */
    public static <T> Result<T> error(String message) {
        return build(ResultCode.FAIL.getCode(), message, null);
    }

    /**
     * 错误响应
     *
     * @param code    响应码
     * @param message 响应消息
     * @param <T>     响应数据类型
     * @return 错误响应结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        return build(code, message, null);
    }

    public static <T> Result<T> badRequest(String message) {
        return build(ResultCode.PARAM_ERROR.getCode(), message, null);
    }

    public static <T> Result<T> unauthorized(String message) {
        return build(ResultCode.NOT_LOGIN.getCode(), message, null);
    }

    public static <T> Result<T> forbidden(String message) {
        return build(ResultCode.NO_PERMISSION.getCode(), message, null);
    }

    public static <T> Result<T> notFound(String message) {
        return build(ResultCode.DATA_NOT_EXIST.getCode(), message, null);
    }

    public static <T> Result<T> systemError(String message) {
        return build(ResultCode.SYSTEM_ERROR.getCode(), message, null);
    }

    public static <T> Result<T> serviceUnavailable(String message) {
        return build(ResultCode.SERVICE_UNAVAILABLE.getCode(), message, null);
    }

    private static <T> Result<T> build(Integer code, String message, T data) {
        return new Result<>(code, message, data, new Date());
    }

    private static String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = normalize(traceId);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = normalize(requestId);
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = normalize(service);
    }
}
