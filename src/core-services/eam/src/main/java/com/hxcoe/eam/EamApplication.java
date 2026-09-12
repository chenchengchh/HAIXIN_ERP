package com.hxcoe.eam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.hxcoe.eam", "com.hxcoe.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class EamApplication {
    public static void main(String[] args) {
        SpringApplication.run(EamApplication.class, args);
    }
}
