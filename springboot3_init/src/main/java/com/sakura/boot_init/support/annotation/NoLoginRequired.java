package com.sakura.boot_init.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 鏍囪鎺ュ彛涓嶉渶瑕佺櫥褰曞嵆鍙闂€? */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoLoginRequired {
}


