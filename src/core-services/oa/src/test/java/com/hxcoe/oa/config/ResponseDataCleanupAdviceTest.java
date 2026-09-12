package com.hxcoe.oa.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.Result;
import org.junit.jupiter.api.Test;

class ResponseDataCleanupAdviceTest {

    private final ResponseDataCleanupAdvice advice = new ResponseDataCleanupAdvice();

    @Test
    void shouldCleanupHtmlCommentForApiResponseStringPayload() {
        ApiResponse<String> body = new ApiResponse<>(200, "成功", "<!--comment-->hello", "trace-1", "request-1", "oa");

        Object cleaned = advice.beforeBodyWrite(body, null, null, null, null, null);

        ApiResponse<?> response = assertInstanceOf(ApiResponse.class, cleaned);
        assertEquals("hello", response.getData());
        assertEquals(200, response.getCode());
        assertEquals("trace-1", response.getTraceId());
    }

    @Test
    void shouldKeepHistoricalResultCleanupCompatible() {
        Result<String> body = new Result<>(0, "成功", "<!--comment-->hello", null, "trace-1", "request-1", "oa");

        Object cleaned = advice.beforeBodyWrite(body, null, null, null, null, null);

        Result<?> response = assertInstanceOf(Result.class, cleaned);
        assertEquals("hello", response.getData());
        assertEquals(0, response.getCode());
        assertEquals("trace-1", response.getTraceId());
    }
}
