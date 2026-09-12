package com.hxcoe.aps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CharacterEncodingFilter;

import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(defaultConfiguration = com.hxcoe.aps.config.FeignConfig.class)
@RestController
public class ApsApplication {

    // 主方法
    public static void main(String[] args) {
        SpringApplication.run(ApsApplication.class, args);
    }

    /**
     * 配置CharacterEncodingFilter
     * 确保所有请求和响应都使用UTF-8编码
     */
    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> characterEncodingFilter() {
        FilterRegistrationBean<CharacterEncodingFilter> registrationBean = new FilterRegistrationBean<>();
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        // 设置编码为UTF-8
        filter.setEncoding("UTF-8");
        // 强制请求使用UTF-8编码
        filter.setForceRequestEncoding(true);
        // 强制响应使用UTF-8编码
        filter.setForceResponseEncoding(true);
        // 对所有URL生效
        registrationBean.addUrlPatterns("/*");
        registrationBean.setFilter(filter);
        // 设置最高优先级
        registrationBean.setOrder(1);
        return registrationBean;
    }
    
    /**
     * 测试端点，用于验证中文编码
     */
    @GetMapping(value = "/test-encoding", produces = "text/plain;charset=UTF-8")
    public String testEncoding() {
        return "测试中文编码：生产线1，件/小时";
    }
    
    /**
     * 测试JSON端点，用于验证中文编码
     */
    @GetMapping(value = "/test-json", produces = "application/json;charset=UTF-8")
    public String testJson() {
        return "{\"resourceName\":\"生产线1\",\"unit\":\"件/小时\"}";
    }
}
