package com.community.edu.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.tenant")
public class TenantProperties {

    private String headerName = "X-Campus-Id";
}
