package com.hxcoe.srm.config;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SrmSyncDataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("primaryDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    /**
     * SRM 同步写入专用数据源（用于写入 PO 镜像与 Inbox，避免业务写入双主）。
     */
    @Bean(name = "srmSyncDataSourceProperties")
    @ConfigurationProperties("srm.sync-datasource")
    public DataSourceProperties srmSyncDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "srmSyncDataSource")
    public DataSource srmSyncDataSource(
        @Qualifier("srmSyncDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder().build();
    }

    /**
     * 主事务管理器（绑定主数据源的 JPA EntityManagerFactory）。
     * 由于 srmSyncTxManager 的存在使 Spring Boot 自动配置回退，不再创建默认
     * transactionManager，导致业务代码中未指定名称的 @Transactional 失效，需显式声明。
     */
    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    /**
     * SRM 同步写入事务管理器（绑定 srmSyncDataSource）。
     */
    @Bean(name = "srmSyncTxManager")
    public PlatformTransactionManager srmSyncTxManager(@Qualifier("srmSyncDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * SRM 同步写入 JDBC 模板（用于幂等插入与镜像 Upsert）。
     */
    @Bean(name = "srmSyncJdbcTemplate")
    public NamedParameterJdbcTemplate srmSyncJdbcTemplate(@Qualifier("srmSyncDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}
