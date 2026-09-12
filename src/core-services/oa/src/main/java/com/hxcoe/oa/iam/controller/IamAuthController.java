package com.hxcoe.oa.iam.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.oa.iam.service.IamAuthService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/iam/auth", "/iam/auth"})
public class IamAuthController {

    @Autowired
    private IamAuthService iamAuthService;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");
        Map<String, Object> res = iamAuthService.login(
                req == null ? null : req.getUsername(),
                req == null ? null : req.getPassword(),
                ip,
                ua,
                traceId
        );
        if (res == null) {
            return ApiResponse.error(401, "用户名或密码错误");
        }
        return ApiResponse.success("成功", res);
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
