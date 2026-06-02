package com.community.edu.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.edu.auth.dto.CampusOption;
import com.community.edu.auth.dto.LoginRequest;
import com.community.edu.auth.dto.LoginResponse;
import com.community.edu.auth.dto.UserInfoResponse;
import com.community.edu.common.context.CurrentUser;
import com.community.edu.common.context.CurrentUserHolder;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.config.AppSecurityProperties;
import com.community.edu.entity.SysCampus;
import com.community.edu.entity.SysPermission;
import com.community.edu.entity.SysRole;
import com.community.edu.entity.SysRolePermission;
import com.community.edu.entity.SysUser;
import com.community.edu.entity.SysUserCampus;
import com.community.edu.entity.SysUserRole;
import com.community.edu.mapper.SysCampusMapper;
import com.community.edu.mapper.SysPermissionMapper;
import com.community.edu.mapper.SysRoleMapper;
import com.community.edu.mapper.SysRolePermissionMapper;
import com.community.edu.mapper.SysUserCampusMapper;
import com.community.edu.mapper.SysUserMapper;
import com.community.edu.mapper.SysUserRoleMapper;
import com.community.edu.security.JwtTokenProvider;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ENABLED = "ENABLED";
    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserCampusMapper userCampusMapper;
    private final SysCampusMapper campusMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AppSecurityProperties securityProperties;
    private final StringRedisTemplate stringRedisTemplate;

    @Transactional
    public LoginResponse login(LoginRequest request, String clientIp) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, request.getUsername())
            .last("LIMIT 1"));
        if (user == null || !ENABLED.equals(user.getStatus())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), normalizePasswordHash(user.getPasswordHash()))) {
            log.warn("login failed: username={}, ip={}", request.getUsername(), clientIp);
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        user.setLastLoginAt(OffsetDateTime.now());
        user.setLastLoginIp(clientIp);
        userMapper.updateById(user);

        CurrentUser currentUser = buildCurrentUser(user);
        return buildLoginResponse(user, currentUser);
    }

    public LoginResponse issueLoginResponse(SysUser user) {
        return buildLoginResponse(user, buildCurrentUser(user));
    }

    public LoginResponse refresh(String refreshToken) {
        JwtTokenProvider.ParsedToken parsedToken = jwtTokenProvider.parse(refreshToken);
        if (parsedToken.getTokenType() != JwtTokenProvider.TokenType.REFRESH) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "刷新Token类型不正确");
        }
        String key = REFRESH_TOKEN_PREFIX + parsedToken.getJwtId();
        String userId = stringRedisTemplate.opsForValue().get(key);
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "刷新Token已失效");
        }
        SysUser user = userMapper.selectById(Long.valueOf(userId));
        if (user == null || !ENABLED.equals(user.getStatus())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号不可用");
        }
        stringRedisTemplate.delete(key);
        return buildLoginResponse(user, buildCurrentUser(user));
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        JwtTokenProvider.ParsedToken parsedToken = jwtTokenProvider.parse(refreshToken);
        if (parsedToken.getTokenType() == JwtTokenProvider.TokenType.REFRESH) {
            stringRedisTemplate.delete(REFRESH_TOKEN_PREFIX + parsedToken.getJwtId());
        }
    }

    public UserInfoResponse currentUserInfo() {
        CurrentUser currentUser = CurrentUserHolder.getRequired();
        SysUser user = userMapper.selectById(currentUser.getUserId());
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return toUserInfo(user, currentUser);
    }

    public CurrentUser buildCurrentUser(SysUser user) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getUserId, user.getId()));
        Set<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toCollection(LinkedHashSet::new));
        List<SysRole> roles = roleIds.isEmpty() ? List.of() : roleMapper.selectBatchIds(roleIds);
        Set<String> roleCodes = roles.stream().map(SysRole::getCode).collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Long> permissionIds = roleIds.isEmpty()
            ? Set.of()
            : rolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds))
                .stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> permissions = permissionIds.isEmpty()
            ? Set.of()
            : permissionMapper.selectBatchIds(permissionIds)
                .stream()
                .filter(permission -> ENABLED.equals(permission.getStatus()))
                .map(SysPermission::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        CampusAccess campusAccess = resolveCampusAccess(user.getId(), roleCodes);
        return CurrentUser.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .accountType(user.getAccountType())
            .roleCodes(roleCodes)
            .permissions(permissions)
            .campusIds(campusAccess.campusIds())
            .defaultCampusId(campusAccess.defaultCampusId())
            .selectedCampusId(campusAccess.defaultCampusId())
            .build();
    }

    private LoginResponse buildLoginResponse(SysUser user, CurrentUser currentUser) {
        String accessToken = jwtTokenProvider.createToken(currentUser, JwtTokenProvider.TokenType.ACCESS);
        String refreshToken = jwtTokenProvider.createToken(currentUser, JwtTokenProvider.TokenType.REFRESH);
        JwtTokenProvider.ParsedToken parsedRefreshToken = jwtTokenProvider.parse(refreshToken);
        Duration ttl = securityProperties.getRefreshTokenTtl();
        stringRedisTemplate.opsForValue().set(
            REFRESH_TOKEN_PREFIX + parsedRefreshToken.getJwtId(),
            String.valueOf(user.getId()),
            ttl
        );
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(securityProperties.getAccessTokenTtl().toSeconds())
            .userInfo(toUserInfo(user, currentUser))
            .build();
    }

    private UserInfoResponse toUserInfo(SysUser user, CurrentUser currentUser) {
        Map<Long, SysCampus> campusMap = currentUser.campusIds().isEmpty()
            ? Map.of()
            : campusMapper.selectBatchIds(currentUser.campusIds()).stream()
                .collect(Collectors.toMap(SysCampus::getId, Function.identity()));
        List<CampusOption> campusList = currentUser.campusIds().stream()
            .map(campusMap::get)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(SysCampus::getId))
            .map(campus -> CampusOption.builder()
                .id(campus.getId())
                .code(campus.getCode())
                .name(campus.getName())
                .shortName(campus.getShortName())
                .isDefault(Objects.equals(campus.getId(), currentUser.getDefaultCampusId()))
                .build())
            .toList();

        return UserInfoResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .phone(user.getPhone())
            .accountType(user.getAccountType())
            .roles(currentUser.roleCodes())
            .permissions(currentUser.permissions())
            .campusList(campusList)
            .defaultCampusId(currentUser.getDefaultCampusId())
            .build();
    }

    private CampusAccess resolveCampusAccess(Long userId, Set<String> roleCodes) {
        if (roleCodes.contains("SUPER_ADMIN")) {
            List<Long> campusIds = campusMapper.selectList(new LambdaQueryWrapper<SysCampus>()
                    .eq(SysCampus::getStatus, ENABLED)
                    .orderByAsc(SysCampus::getId))
                .stream()
                .map(SysCampus::getId)
                .toList();
            return new CampusAccess(campusIds, campusIds.isEmpty() ? null : campusIds.get(0));
        }

        List<SysUserCampus> userCampuses = userCampusMapper.selectList(new LambdaQueryWrapper<SysUserCampus>()
            .eq(SysUserCampus::getUserId, userId)
            .orderByDesc(SysUserCampus::getIsDefault)
            .orderByAsc(SysUserCampus::getCampusId));
        List<Long> campusIds = userCampuses.stream().map(SysUserCampus::getCampusId).distinct().toList();
        Long defaultCampusId = userCampuses.stream()
            .filter(item -> Boolean.TRUE.equals(item.getIsDefault()))
            .map(SysUserCampus::getCampusId)
            .findFirst()
            .orElse(campusIds.isEmpty() ? null : campusIds.get(0));
        return new CampusAccess(campusIds, defaultCampusId);
    }

    private String normalizePasswordHash(String passwordHash) {
        if (passwordHash != null && passwordHash.startsWith("{bcrypt}")) {
            return passwordHash.substring("{bcrypt}".length());
        }
        return passwordHash;
    }

    private record CampusAccess(List<Long> campusIds, Long defaultCampusId) {
    }
}
