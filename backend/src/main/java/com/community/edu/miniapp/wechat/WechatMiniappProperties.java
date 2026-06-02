package com.community.edu.miniapp.wechat;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.wechat.miniapp")
public class WechatMiniappProperties {

    private String appId;
    private String appSecret;
    private boolean mockEnabled = true;
}
