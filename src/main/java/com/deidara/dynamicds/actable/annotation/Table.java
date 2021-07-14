package com.deidara.dynamicds.actable.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标注该注解的实体类都会被映射成hive表
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface Table {
    String value() default "";
    String comment() default "";
    String ds() default "";
}
