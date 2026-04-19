package com.sakura.boot_init;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 主类，项目启动入口
 *
 * @author sakura
 * @from sakura
 */
@Modulithic(systemName = "springboot3_init")
@SpringBootApplication
@MapperScan("com.sakura.boot_init.**.repository")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class SakuraApplication {

    public static void main(String[] args) {
        SpringApplication.run(SakuraApplication.class, args);
    }
}
