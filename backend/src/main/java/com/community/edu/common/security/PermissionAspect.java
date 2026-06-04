package com.community.edu.common.security;

import com.community.edu.common.context.CurrentUser;

/**
 * 权限切面。拦截带@RequirePermission注解的方法并进行权限校验。
 */
import com.community.edu.common.context.CurrentUserHolder;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(requirePermission)")
    public void checkPermission(RequirePermission requirePermission) {
        CurrentUser currentUser = CurrentUserHolder.getRequired();
        String permission = requirePermission.value();
        if (!currentUser.hasPermission(permission)) {
            log.warn("permission denied: userId={}, permission={}", currentUser.getUserId(), permission);
            throw new BizException(ErrorCode.FORBIDDEN, "缺少权限：" + permission);
        }
    }
}
