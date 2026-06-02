package com.community.edu.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.edu.admin.dto.ResetPasswordRequest;
import com.community.edu.admin.dto.UserQuery;
import com.community.edu.admin.dto.UserRequest;
import com.community.edu.admin.dto.UserResponse;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.common.response.PageResponse;
import com.community.edu.entity.SysUser;
import com.community.edu.entity.SysUserCampus;
import com.community.edu.entity.SysUserRole;
import com.community.edu.mapper.SysUserCampusMapper;
import com.community.edu.mapper.SysUserMapper;
import com.community.edu.mapper.SysUserRoleMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserCampusMapper userCampusMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResponse<UserResponse> page(UserQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
            .eq(StringUtils.hasText(query.getAccountType()), SysUser::getAccountType, query.getAccountType())
            .eq(StringUtils.hasText(query.getStatus()), SysUser::getStatus, query.getStatus())
            .and(StringUtils.hasText(query.getKeyword()), item -> item
                .like(SysUser::getUsername, query.getKeyword())
                .or()
                .like(SysUser::getRealName, query.getKeyword())
                .or()
                .like(SysUser::getPhone, query.getKeyword()))
            .orderByDesc(SysUser::getId);
        Page<SysUser> page = userMapper.selectPage(Page.of(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords().stream().map(this::toResponse).toList(),
            page.getTotal(), page.getCurrent(), page.getSize());
    }

    public UserResponse detail(Long id) {
        return toResponse(getRequired(id));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        SysUser user = new SysUser();
        apply(user, request);
        user.setPasswordHash(encode(StringUtils.hasText(request.getPassword()) ? request.getPassword() : DEFAULT_PASSWORD));
        if (!StringUtils.hasText(user.getStatus())) {
            user.setStatus("ENABLED");
        }
        userMapper.insert(user);
        replaceRoles(user.getId(), request.getRoleIds(), request.getDefaultCampusId());
        replaceCampuses(user.getId(), request.getCampusIds(), request.getDefaultCampusId(), request.getAccountType());
        return detail(user.getId());
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        SysUser user = getRequired(id);
        apply(user, request);
        userMapper.updateById(user);
        replaceRoles(id, request.getRoleIds(), request.getDefaultCampusId());
        replaceCampuses(id, request.getCampusIds(), request.getDefaultCampusId(), request.getAccountType());
        return detail(id);
    }

    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        SysUser user = getRequired(id);
        user.setPasswordHash(encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        SysUser user = getRequired(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    private UserResponse toResponse(SysUser user) {
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, user.getId()))
            .stream()
            .map(SysUserRole::getRoleId)
            .toList();
        List<Long> campusIds = userCampusMapper.selectList(new LambdaQueryWrapper<SysUserCampus>()
                .eq(SysUserCampus::getUserId, user.getId()))
            .stream()
            .map(SysUserCampus::getCampusId)
            .toList();
        return UserResponse.from(user, roleIds, campusIds);
    }

    private SysUser getRequired(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private void apply(SysUser user, UserRequest request) {
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setAccountType(request.getAccountType());
        user.setStatus(request.getStatus());
    }

    private void replaceRoles(Long userId, List<Long> roleIds, Long defaultCampusId) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        roleIds.stream().distinct().forEach(roleId -> {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            relation.setCampusId(defaultCampusId);
            userRoleMapper.insert(relation);
        });
    }

    private void replaceCampuses(Long userId, List<Long> campusIds, Long defaultCampusId, String accountType) {
        userCampusMapper.delete(new LambdaQueryWrapper<SysUserCampus>().eq(SysUserCampus::getUserId, userId));
        if (campusIds == null || campusIds.isEmpty()) {
            return;
        }
        campusIds.stream().distinct().forEach(campusId -> {
            SysUserCampus relation = new SysUserCampus();
            relation.setUserId(userId);
            relation.setCampusId(campusId);
            relation.setRelationType(accountType == null ? "ADMIN" : accountType.replace("CAMPUS_", ""));
            relation.setIsDefault(campusId.equals(defaultCampusId) || (defaultCampusId == null && campusIds.indexOf(campusId) == 0));
            userCampusMapper.insert(relation);
        });
    }

    private String encode(String rawPassword) {
        return "{bcrypt}" + passwordEncoder.encode(rawPassword);
    }
}
