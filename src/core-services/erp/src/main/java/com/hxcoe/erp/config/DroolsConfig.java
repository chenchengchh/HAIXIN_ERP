package com.hxcoe.erp.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Drools规则引擎配置类
 * 初始化Drools规则引擎，加载规则文件
 */
@Configuration
public class DroolsConfig {

    /**
     * 创建KieServices实例
     */
    @Bean
    public KieServices kieServices() {
        return KieServices.Factory.get();
    }

    /**
     * 创建KieContainer实例
     * 用于加载和管理规则
     * 暂时禁用规则加载，因为规则文件中引用了不存在的类
     */
    @Bean
    public KieContainer kieContainer(KieServices kieServices) {
        // 创建KieFileSystem
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        
        // 暂时不加载规则文件，因为规则文件中引用了不存在的类
        // kieFileSystem.write(ResourceFactory.newClassPathResource("rules/erp-rules.drl"));
        
        // 构建KieModule
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        
        // 创建KieContainer
        KieModule kieModule = kieBuilder.getKieModule();
        return kieServices.newKieContainer(kieModule.getReleaseId());
    }
}
