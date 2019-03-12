package com.yuxiang.li.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义controller 注解
 */
@Target(value = {ElementType.TYPE})//使用目标
@Retention(RetentionPolicy.RUNTIME)
public @interface EasyController {
    String value() default "";
}
