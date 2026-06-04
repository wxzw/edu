package com.community.edu.config;

import java.time.Duration;

/**
 * 应用安全属性。配置JWT密钥、Token有效期等安全参数。
 */
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private String jwtSecret;
    private Duration accessTokenTtl = Duration.ofHours(2);
    private Duration refreshTokenTtl = Duration.ofDays(7);
    private String defaultPassword = "123456";
}
