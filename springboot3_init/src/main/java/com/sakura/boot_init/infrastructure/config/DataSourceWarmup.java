package com.sakura.boot_init.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 数据库连接池启动预热组件。
 */
@Slf4j
@Order(0)
@Component
@RequiredArgsConstructor
public class DataSourceWarmup implements ApplicationRunner {

    private final DataSource dataSource;

    /**
     * 应用启动完成后主动触发一次连接获取，避免首个业务请求承担连接池初始化成本。
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection ignored = dataSource.getConnection()) {
            log.info("数据库连接池预热完成");
        }
    }
}
