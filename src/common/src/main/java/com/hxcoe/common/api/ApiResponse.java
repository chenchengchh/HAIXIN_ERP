package com.hxcoe.common.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hxcoe.common.web.RequestContextSupport;
import java.io.Serializable;
import org.springframework.util.StringUtils;

/**
 * 统一API响应体（对齐前端约定：code/msg/data）。
 *
 * @param <T> 响应数据类型
 */
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;
    @JsonProperty("msg")
    @JsonAlias("message")
    private String msg;
    private T data;
    @JsonProperty("traceId")
    @JsonAlias("trace_id")
    private String traceId;
    @JsonProperty("requestId")
    @JsonAlias("request_id")
    private String requestId;
    @JsonProperty("service")
    private String service;

    public ApiResponse() {
    }

    public ApiResponse(Integer code, String msg, T data) {
        this(
                code,
                msg,
                data,
                RequestContextSupport.currentTraceId(),
                RequestContextSupport.currentRequestId(),
                RequestContextSupport.currentService()
        );
    }

    public ApiResponse(Integer code, String msg, T data, String traceId, String requestId, String service) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.traceId = normalize(traceId);
        this.requestId = normalize(requestId);
        this.service = normalize(service);
    }

    @JsonProperty("message")
    public String getMessage() {
        return msg;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.msg = message;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "请求成功", data);
    }

    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(200, msg == null || msg.isBlank() ? "请求成功" : msg, data);
    }

    public static <T> ApiResponse<T> error(Integer code, String msg) {
        int normalized = code == null ? 500 : code;
        String normalizedMsg = msg == null || msg.isBlank() ? "请求失败" : msg;
        return new ApiResponse<>(normalized, normalizedMsg, null);
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
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

