package com.community.edu.common.security;

import java.lang.annotation.ElementType;

/**
 * 权限要求注解。用于标记需要特定权限的方法。
 */
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    String value();
}
