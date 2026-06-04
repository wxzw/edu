package com.community.edu.security;

import com.community.edu.common.context.CurrentUser;

/**
 * JWT Token 提供器。负责创建和解析访问Token及刷新Token。
 */
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.config.AppSecurityProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final AppSecurityProperties securityProperties;
    private final ObjectMapper objectMapper;

    public String createToken(CurrentUser currentUser, TokenType tokenType) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(tokenType == TokenType.ACCESS
            ? securityProperties.getAccessTokenTtl()
            : securityProperties.getRefreshTokenTtl());

        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", currentUser.getUsername());
        payload.put("uid", currentUser.getUserId());
        payload.put("realName", currentUser.getRealName());
        payload.put("accountType", currentUser.getAccountType());
        payload.put("roles", currentUser.roleCodes());
        payload.put("permissions", currentUser.permissions());
        payload.put("campusIds", currentUser.campusIds());
        payload.put("defaultCampusId", currentUser.getDefaultCampusId());
        payload.put("typ", tokenType.name());
        payload.put("jti", UUID.randomUUID().toString());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        String unsignedToken = base64Json(header) + "." + base64Json(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public ParsedToken parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BizException(ErrorCode.UNAUTHORIZED, "Token格式不正确");
            }
            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsignedToken);
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                throw new BizException(ErrorCode.UNAUTHORIZED, "Token签名无效");
            }

            Map<String, Object> payload = objectMapper.readValue(
                URL_DECODER.decode(parts[1]),
                new TypeReference<>() {
                }
            );
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= exp) {
                throw new BizException(ErrorCode.UNAUTHORIZED, "Token已过期");
            }

            CurrentUser currentUser = CurrentUser.builder()
                .userId(toLong(payload.get("uid")))
                .username((String) payload.get("sub"))
                .realName((String) payload.get("realName"))
                .accountType((String) payload.get("accountType"))
                .roleCodes(Set.copyOf(toStringList(payload.get("roles"))))
                .permissions(Set.copyOf(toStringList(payload.get("permissions"))))
                .campusIds(toLongList(payload.get("campusIds")))
                .defaultCampusId(toLong(payload.get("defaultCampusId")))
                .build();

            return new ParsedToken(
                currentUser,
                TokenType.valueOf((String) payload.get("typ")),
                (String) payload.get("jti"),
                exp
            );
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "Token解析失败");
        }
    }

    private String base64Json(Map<String, Object> value) {
        try {
            return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize JWT", ex);
        }
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(securityProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return URL_ENCODER.encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to sign JWT", ex);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
        if (leftBytes.length != rightBytes.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < leftBytes.length; i++) {
            result |= leftBytes[i] ^ rightBytes[i];
        }
        return result == 0;
    }

    @SuppressWarnings("unchecked")
    private List<String> toStringList(Object value) {
        if (value == null) {
            return List.of();
        }
        return ((List<?>) value).stream().map(String::valueOf).toList();
    }

    @SuppressWarnings("unchecked")
    private List<Long> toLongList(Object value) {
        if (value == null) {
            return List.of();
        }
        return ((List<?>) value).stream().map(this::toLong).toList();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    public enum TokenType {
        ACCESS,
        REFRESH
    }

    @Getter
    public static class ParsedToken {
        private final CurrentUser currentUser;
        private final TokenType tokenType;
        private final String jwtId;
        private final long expiresAtEpochSeconds;

        public ParsedToken(CurrentUser currentUser, TokenType tokenType, String jwtId, long expiresAtEpochSeconds) {
            this.currentUser = currentUser;
            this.tokenType = tokenType;
            this.jwtId = jwtId;
            this.expiresAtEpochSeconds = expiresAtEpochSeconds;
        }
    }
}
