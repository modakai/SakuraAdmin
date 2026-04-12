package com.sakura.boot_init.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 鏉冮檺鏍￠獙
 *
 * @author sakura
 * @from sakura
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 蹇呴』鏈夋煇涓鑹?     *
     * @return
     */
    String mustRole() default "";

}



