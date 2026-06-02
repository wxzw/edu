package com.community.edu.miniapp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.edu.auth.AuthService;
import com.community.edu.auth.dto.LoginResponse;
import com.community.edu.common.context.CurrentUser;
import com.community.edu.common.context.CurrentUserHolder;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.entity.SysUser;
import com.community.edu.mapper.SysUserMapper;
import com.community.edu.miniapp.dto.MiniappIdentityResponse;
import com.community.edu.miniapp.dto.MiniappLoginRequest;
import com.community.edu.miniapp.dto.MiniappLoginResponse;
import com.community.edu.miniapp.dto.MiniappMeResponse;
import com.community.edu.miniapp.dto.MiniappSelectIdentityRequest;
import com.community.edu.miniapp.wechat.WechatMiniappClient;
import com.community.edu.miniapp.wechat.WechatMiniappClient.WechatLoginContext;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiniappAuthService {

    private static final String ENABLED = "ENABLED";

    private final SysUserMapper userMapper;
    private final AuthService authService;
    private final MiniappIdentityService identityService;
    private final MiniappIdentityScopeService identityScopeService;
    private final WechatMiniappClient wechatMiniappClient;

    @Transactional
    public MiniappLoginResponse login(MiniappLoginRequest request, String clientIp) {
        WechatLoginContext wechatContext = wechatMiniappClient.resolveLoginContext(request);
        String roleHint = identityService.normalizeIdentityType(request.getRoleHint());

        SysUser boundUser = findByOpenId(wechatContext.getOpenId());
        if (boundUser != null) {
            validateEnabled(boundUser);
            MiniappLoginBundle bundle = buildBundle(boundUser, roleHint);
            touchLogin(boundUser, clientIp);
            return toLoginResponse(bundle);
        }

        if (!StringUtils.hasText(wechatContext.getPhone())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "Phone authorization is required for first miniapp login");
        }

        MiniappIdentityCandidate candidate = resolvePrebuiltAccount(wechatContext.getPhone(), roleHint);
        bindWechat(candidate.user(), wechatContext);
        touchLogin(candidate.user(), clientIp);
        return toLoginResponse(buildBundle(candidate.user(), roleHint));
    }

    public MiniappMeResponse me() {
        CurrentUser currentUser = CurrentUserHolder.getRequired();
        SysUser user = requiredUser(currentUser.getUserId());
        List<MiniappIdentityResponse> identities = identityService.listAvailableIdentities(user, currentUser.campusIds());
        MiniappIdentityResponse selectedIdentity = identityScopeService.resolveFromHeadersOrDefault(user, currentUser);
        return MiniappMeResponse.builder()
            .userInfo(authService.currentUserInfo())
            .availableIdentities(identities)
            .selectedIdentity(selectedIdentity)
            .build();
    }

    public MiniappMeResponse selectIdentity(MiniappSelectIdentityRequest request) {
        CurrentUser currentUser = CurrentUserHolder.getRequired();
        SysUser user = requiredUser(currentUser.getUserId());
        MiniappIdentityResponse selectedIdentity = identityService.resolveSelectedIdentity(
            user,
            currentUser,
            request.getIdentityType(),
            request.getIdentityId()
        );
        return MiniappMeResponse.builder()
            .userInfo(authService.currentUserInfo())
            .availableIdentities(identityService.listAvailableIdentities(user, currentUser.campusIds()))
            .selectedIdentity(selectedIdentity)
            .build();
    }

    private MiniappIdentityCandidate resolvePrebuiltAccount(String phone, String roleHint) {
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getPhone, phone)
            .eq(SysUser::getStatus, ENABLED)
            .in(SysUser::getAccountType, List.of(
                MiniappIdentityService.TEACHER,
                MiniappIdentityService.GUARDIAN,
                MiniappIdentityService.STUDENT
            ));
        if (StringUtils.hasText(roleHint)) {
            query.eq(SysUser::getAccountType, roleHint);
        }

        List<MiniappIdentityCandidate> candidates = userMapper.selectList(query).stream()
            .map(user -> {
                try {
                    return buildIdentityCandidate(user, roleHint);
                } catch (BizException ex) {
                    log.debug("skip miniapp candidate without valid identity: userId={}, reason={}", user.getId(), ex.getMessage());
                    return null;
                }
            })
            .filter(bundle -> bundle != null)
            .toList();

        if (candidates.isEmpty()) {
            throw new BizException(ErrorCode.NOT_FOUND, "No prebuilt miniapp account matched this phone");
        }
        if (candidates.size() > 1) {
            throw new BizException(ErrorCode.CONFLICT, "Phone matched multiple miniapp accounts; roleHint is required");
        }
        return candidates.get(0);
    }

    private MiniappLoginBundle buildBundle(SysUser user, String roleHint) {
        MiniappIdentityCandidate candidate = buildIdentityCandidate(user, roleHint);
        LoginResponse tokenResponse = authService.issueLoginResponse(user);
        return new MiniappLoginBundle(user, tokenResponse, candidate.identities(), candidate.selectedIdentity());
    }

    private MiniappIdentityCandidate buildIdentityCandidate(SysUser user, String roleHint) {
        validateEnabled(user);
        CurrentUser currentUser = authService.buildCurrentUser(user);
        List<MiniappIdentityResponse> identities = identityService.listAvailableIdentities(user, currentUser.campusIds());
        MiniappIdentityResponse selectedIdentity = identityService.pickDefaultIdentity(identities, roleHint);
        return new MiniappIdentityCandidate(user, identities, selectedIdentity);
    }

    private MiniappLoginResponse toLoginResponse(MiniappLoginBundle bundle) {
        LoginResponse tokenResponse = bundle.tokenResponse();
        return MiniappLoginResponse.builder()
            .accessToken(tokenResponse.getAccessToken())
            .refreshToken(tokenResponse.getRefreshToken())
            .expiresIn(tokenResponse.getExpiresIn())
            .userInfo(tokenResponse.getUserInfo())
            .availableIdentities(bundle.identities())
            .selectedIdentity(bundle.selectedIdentity())
            .build();
    }

    private SysUser findByOpenId(String openId) {
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getWxOpenId, openId)
            .last("LIMIT 1"));
    }

    private SysUser requiredUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        validateEnabled(user);
        return user;
    }

    private void bindWechat(SysUser user, WechatLoginContext wechatContext) {
        user.setWxOpenId(wechatContext.getOpenId());
        if (StringUtils.hasText(wechatContext.getUnionId())) {
            user.setWxUnionId(wechatContext.getUnionId());
        }
        try {
            userMapper.updateById(user);
        } catch (DuplicateKeyException ex) {
            throw new BizException(ErrorCode.CONFLICT, "Wechat openid has already been bound");
        }
    }

    private void touchLogin(SysUser user, String clientIp) {
        user.setLastLoginAt(OffsetDateTime.now());
        user.setLastLoginIp(clientIp);
        userMapper.updateById(user);
    }

    private void validateEnabled(SysUser user) {
        if (!ENABLED.equals(user.getStatus())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "Account is disabled");
        }
    }

    private record MiniappLoginBundle(
        SysUser user,
        LoginResponse tokenResponse,
        List<MiniappIdentityResponse> identities,
        MiniappIdentityResponse selectedIdentity
    ) {
    }

    private record MiniappIdentityCandidate(
        SysUser user,
        List<MiniappIdentityResponse> identities,
        MiniappIdentityResponse selectedIdentity
    ) {
    }
}
