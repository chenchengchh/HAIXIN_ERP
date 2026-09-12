package com.hxcoe.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ERP鏈嶅姟鍚姩锟? */
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
// defaultConfiguration=FeignConfig：为所有Feign客户端注册Authorization头中继拦截器，
// 使服务间调用（如ERP→OA提交审批）携带当前用户JWT，避免下游401
@EnableFeignClients(defaultConfiguration = com.hxcoe.erp.config.FeignConfig.class)
@EnableScheduling
public class ErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpApplication.class, args);
    }

}

