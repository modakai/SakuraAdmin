package com.sakura.boot_init;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 涓荤被锛堥」鐩惎鍔ㄥ叆鍙ｏ級
 *
 * @author sakura
 * @from sakura
 */
@SpringBootApplication
@MapperScan({"com.sakura.boot_init.user.repository", "com.sakura.boot_init.dict.repository"})
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class SakuraApplication {

    public static void main(String[] args) {
        SpringApplication.run(SakuraApplication.class, args);
    }

}



