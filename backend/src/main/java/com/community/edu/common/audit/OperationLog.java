package com.community.edu.common.audit;

import java.lang.annotation.ElementType;

/**
 * 操作日志注解。用于标记需要记录操作日志的方法。
 */
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    String module();

    String operation();

    String bizType() default "";
}
