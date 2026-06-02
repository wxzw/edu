package com.community.edu.miniapp.wechat;

import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.miniapp.dto.MiniappLoginRequest;
import java.time.Duration;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class WechatMiniappClient {

    private static final String CODE2_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String PHONE_NUMBER_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber";
    private static final String ACCESS_TOKEN_CACHE_KEY = "wechat:miniapp:access-token";

    private final WechatMiniappProperties properties;
    private final StringRedisTemplate stringRedisTemplate;
    private final RestClient restClient = RestClient.create();

    public WechatLoginContext resolveLoginContext(MiniappLoginRequest request) {
        if (properties.isMockEnabled()) {
            return mockContext(request);
        }
        validateRealConfig();
        if (!StringUtils.hasText(request.getLoginCode())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "loginCode is required");
        }

        Map<?, ?> session = code2Session(request.getLoginCode());
        String openId = stringValue(session.get("openid"));
        if (!StringUtils.hasText(openId)) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "Wechat login did not return openid");
        }
        String phone = StringUtils.hasText(request.getPhoneCode())
            ? fetchPhoneNumber(request.getPhoneCode())
            : null;

        return WechatLoginContext.builder()
            .openId(openId)
            .unionId(stringValue(session.get("unionid")))
            .sessionKey(stringValue(session.get("session_key")))
            .phone(phone)
            .build();
    }

    private WechatLoginContext mockContext(MiniappLoginRequest request) {
        String phone = normalizeBlank(request.getMockPhone());
        String openId = normalizeBlank(request.getMockOpenId());
        if (!StringUtils.hasText(openId) && StringUtils.hasText(phone)) {
            openId = "mock_" + phone;
        }
        if (!StringUtils.hasText(openId)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "mockOpenId or mockPhone is required in mock mode");
        }
        return WechatLoginContext.builder()
            .openId(openId)
            .unionId(normalizeBlank(request.getMockUnionId()))
            .phone(phone)
            .sessionKey("mock-session")
            .build();
    }

    private Map<?, ?> code2Session(String loginCode) {
        String url = UriComponentsBuilder.fromHttpUrl(CODE2_SESSION_URL)
            .queryParam("appid", properties.getAppId())
            .queryParam("secret", properties.getAppSecret())
            .queryParam("js_code", loginCode)
            .queryParam("grant_type", "authorization_code")
            .build()
            .toUriString();
        Map<?, ?> response = restClient.get().uri(url).retrieve().body(Map.class);
        assertWechatSuccess(response, "code2Session");
        return response == null ? Map.of() : response;
    }

    private String fetchPhoneNumber(String phoneCode) {
        String accessToken = resolveAccessToken();
        String url = UriComponentsBuilder.fromHttpUrl(PHONE_NUMBER_URL)
            .queryParam("access_token", accessToken)
            .build()
            .toUriString();
        Map<?, ?> response = restClient.post()
            .uri(url)
            .body(Map.of("code", phoneCode))
            .retrieve()
            .body(Map.class);
        assertWechatSuccess(response, "getPhoneNumber");
        Object phoneInfo = response == null ? null : response.get("phone_info");
        if (!(phoneInfo instanceof Map<?, ?> phoneInfoMap)) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "Wechat phone response is invalid");
        }
        String phone = stringValue(phoneInfoMap.get("purePhoneNumber"));
        if (!StringUtils.hasText(phone)) {
            phone = stringValue(phoneInfoMap.get("phoneNumber"));
        }
        if (!StringUtils.hasText(phone)) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "Wechat phone response did not return phone");
        }
        return phone;
    }

    private String resolveAccessToken() {
        String cachedToken = stringRedisTemplate.opsForValue().get(ACCESS_TOKEN_CACHE_KEY);
        if (StringUtils.hasText(cachedToken)) {
            return cachedToken;
        }
        String url = UriComponentsBuilder.fromHttpUrl(ACCESS_TOKEN_URL)
            .queryParam("grant_type", "client_credential")
            .queryParam("appid", properties.getAppId())
            .queryParam("secret", properties.getAppSecret())
            .build()
            .toUriString();
        Map<?, ?> response = restClient.get().uri(url).retrieve().body(Map.class);
        assertWechatSuccess(response, "accessToken");
        String token = stringValue(response == null ? null : response.get("access_token"));
        if (!StringUtils.hasText(token)) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "Wechat access token response is invalid");
        }
        long expiresIn = response.get("expires_in") instanceof Number number ? number.longValue() : 7200L;
        stringRedisTemplate.opsForValue().set(ACCESS_TOKEN_CACHE_KEY, token, Duration.ofSeconds(Math.max(60L, expiresIn - 120L)));
        return token;
    }

    private void assertWechatSuccess(Map<?, ?> response, String operation) {
        Object errCodeValue = response == null ? null : response.get("errcode");
        int errCode = errCodeValue instanceof Number number ? number.intValue() : 0;
        if (errCode != 0) {
            String errMsg = stringValue(response.get("errmsg"));
            log.warn("wechat miniapp {} failed: errcode={}, errmsg={}", operation, errCode, errMsg);
            throw new BizException(ErrorCode.UNAUTHORIZED, "Wechat " + operation + " failed");
        }
    }

    private void validateRealConfig() {
        if (!StringUtils.hasText(properties.getAppId()) || !StringUtils.hasText(properties.getAppSecret())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Wechat miniapp appId and appSecret are not configured");
        }
    }

    private String normalizeBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @Getter
    @Builder
    public static class WechatLoginContext {
        private String openId;
        private String unionId;
        private String sessionKey;
        private String phone;
    }
}
