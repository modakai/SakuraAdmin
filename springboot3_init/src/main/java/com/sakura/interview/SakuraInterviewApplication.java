package com.sakura.interview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 主类（项目启动入口）
 *
 * @author sakura
 * @from sakura
 */
@SpringBootApplication
@MapperScan("com.sakura.interview.infra.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class SakuraInterviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(SakuraInterviewApplication.class, args);
    }

}
