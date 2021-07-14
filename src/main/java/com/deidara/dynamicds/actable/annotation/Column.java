package com.deidara.dynamicds.actable.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Column {
    String name() default "";
    String type() default "";
    String comment() default "";
    boolean primaryKey() default false;
    boolean nullable() default true;
    String defaultValue() default "NULL";
    boolean autoUpdate() default false;
}
