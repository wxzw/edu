package com.community.edu.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Web 请求工具类。
 */
public final class WebUtils {

    private WebUtils() {}

    /**
     * 获取当前请求的客户端 IP 地址。
     * 优先从 X-Forwarded-For 头获取（支持代理），其次从 RemoteAddr 获取。
     */
    public static String clientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest request = attrs.getRequest();
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader != null && !xfHeader.isEmpty()) {
                return xfHeader.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "unknown";
    }
}
