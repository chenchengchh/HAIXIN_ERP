package com.hxcoe.hr.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 数据初始化类，用于在应用启动时加载初始化数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final DataSource dataSource;

    public DataInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("开始加载HR系统初始化数据...");
        
        try (Connection connection = dataSource.getConnection()) {
            // 创建编码资源
            EncodedResource resource = new EncodedResource(new ClassPathResource("init-data.sql"));
            
            // 执行SQL脚本，设置continueOnError为true，遇到错误继续执行
            // 注意参数顺序：commentPrefix, separator, blockCommentStart, blockCommentEnd
            ScriptUtils.executeSqlScript(
                connection,
                resource,
                true,   // continueOnError：遇到错误继续执行
                true,   // ignoreFailedDrops：忽略失败的删除语句
                "--",   // commentPrefix：单行注释前缀
                ";",    // separator：语句分隔符
                "/*",   // blockCommentStartDelimiter：块注释开始
                "*/"    // blockCommentEndDelimiter：块注释结束
            );
            logger.info("HR系统初始化数据加载成功！");
        } catch (Exception e) {
            logger.error("HR系统初始化数据加载失败：{}", e.getMessage());
            // 如果是重复插入导致的失败，忽略该错误
            if (e.getMessage().contains("Duplicate entry")) {
                logger.warn("初始化数据可能已经存在，跳过重复插入");
                return; // 直接返回，不抛出异常
            }
            throw e;
        }
    }
}