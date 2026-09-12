package com.hxcoe.oa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应数据清理处理器
 * 用于清理响应数据中的非法属性名，确保返回给前端的数据格式正确
 */
@ControllerAdvice
public class ResponseDataCleanupAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 是否支持处理当前响应
     * @param returnType 返回类型
     * @param converterType 消息转换器类型
     * @return 是否支持
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * 处理响应体
     * @param body 响应体
     * @param returnType 返回类型
     * @param selectedContentType 选中的内容类型
     * @param selectedConverterType 选中的消息转换器类型
     * @param request 请求
     * @param response 响应
     * @return 处理后的响应体
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, 
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, 
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof Result<?>) {
            Result<?> result = (Result<?>) body;
            Object cleaned = cleanupStringPayload(result.getData());
            if (cleaned != result.getData()) {
                return new Result<>(
                        result.getCode(),
                        result.getMessage(),
                        cleaned,
                        result.getTimestamp(),
                        result.getTraceId(),
                        result.getRequestId(),
                        result.getService()
                );
            }
        }

        if (body instanceof ApiResponse<?>) {
            ApiResponse<?> apiResponse = (ApiResponse<?>) body;
            Object cleaned = cleanupStringPayload(apiResponse.getData());
            if (cleaned != apiResponse.getData()) {
                return new ApiResponse<>(
                        apiResponse.getCode(),
                        apiResponse.getMsg(),
                        cleaned,
                        apiResponse.getTraceId(),
                        apiResponse.getRequestId(),
                        apiResponse.getService()
                );
            }
        }

        return body;
    }

    private Object cleanupStringPayload(Object data) {
        if (!(data instanceof String strData)) {
            return data;
        }
        String cleanedStr = strData.replaceAll("<!--[\\s\\S]*?-->", "");
        return strData.equals(cleanedStr) ? data : cleanedStr;
    }
}
