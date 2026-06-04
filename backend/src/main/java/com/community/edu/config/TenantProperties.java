package com.community.edu.config;

import lombok.Getter;

/**
 * 多租户属性。配置多租户相关的属性参数。
 */
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.tenant")
public class TenantProperties {

    private String headerName = "X-Campus-Id";
}
