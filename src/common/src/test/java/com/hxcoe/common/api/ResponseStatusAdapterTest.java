package com.hxcoe.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ResponseStatusAdapterTest {

    @Test
    void shouldTreatLegacyAndApiResponseSuccessCodesAsSuccess() {
        assertTrue(ResponseStatusAdapter.isSuccess(0));
        assertTrue(ResponseStatusAdapter.isSuccess(200));
        assertTrue(ResponseStatusAdapter.isSuccess(Map.of("code", 0, "message", "成功")));
        assertTrue(ResponseStatusAdapter.isSuccess(Map.of("code", 200, "msg", "请求成功")));
    }

    @Test
    void shouldTreatOtherCodesAsFailure() {
        assertFalse(ResponseStatusAdapter.isSuccess(Map.of("code", 400, "msg", "参数错误")));
        assertFalse(ResponseStatusAdapter.isSuccess(Map.of()));
        assertFalse(ResponseStatusAdapter.isSuccess((Integer) null));
    }

    @Test
    void shouldReadMsgAndMessageFields() {
        assertEquals("请求成功", ResponseStatusAdapter.extractMessage(Map.of("msg", "请求成功")));
        assertEquals("成功", ResponseStatusAdapter.extractMessage(Map.of("message", "成功")));
    }
}
