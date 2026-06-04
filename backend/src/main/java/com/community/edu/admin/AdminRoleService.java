package com.community.edu.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.edu.admin.dto.PermissionNode;
import com.community.edu.admin.dto.RolePermissionRequest;
import com.community.edu.admin.dto.RoleQuery;
import com.community.edu.admin.dto.RoleRequest;
import com.community.edu.admin.dto.RoleResponse;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.common.response.PageResponse;
import com.community.edu.entity.SysPermission;
import com.community.edu.entity.SysRole;
import com.community.edu.entity.SysRolePermission;
import com.community.edu.mapper.SysPermissionMapper;
import com.community.edu.mapper.SysRoleMapper;
import com.community.edu.mapper.SysRolePermissionMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 角色权限管理服务。处理角色的CRUD及权限授权。
 */
@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    public PageResponse<RoleResponse> page(RoleQuery query) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
            .eq(StringUtils.hasText(query.getStatus()), SysRole::getStatus, query.getStatus())
            .and(StringUtils.hasText(query.getKeyword()), item -> item
                .like(SysRole::getCode, query.getKeyword())
                .or()
                .like(SysRole::getName, query.getKeyword()))
            .orderByAsc(SysRole::getId);
        Page<SysRole> page = roleMapper.selectPage(Page.of(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords().stream().map(role -> RoleResponse.from(role, permissionIds(role.getId()))).toList(),
            page.getTotal(), page.getCurrent(), page.getSize());
    }

    public RoleResponse detail(Long id) {
        SysRole role = getRequired(id);
        return RoleResponse.from(role, permissionIds(id));
    }

    @Transactional
    public RoleResponse create(RoleRequest request) {
        SysRole role = new SysRole();
        apply(role, request);
        if (!StringUtils.hasText(role.getScopeType())) {
            role.setScopeType("SYSTEM");
        }
        if (!StringUtils.hasText(role.getDataScope())) {
            role.setDataScope("CAMPUS");
        }
        if (!StringUtils.hasText(role.getStatus())) {
            role.setStatus("ENABLED");
        }
        roleMapper.insert(role);
        replacePermissions(role.getId(), request.getPermissionIds());
        return detail(role.getId());
    }

    @Transactional
    public RoleResponse update(Long id, RoleRequest request) {
        SysRole role = getRequired(id);
        apply(role, request);
        roleMapper.updateById(role);
        if (request.getPermissionIds() != null) {
            replacePermissions(id, request.getPermissionIds());
        }
        return detail(id);
    }

    @Transactional
    public void grant(Long id, RolePermissionRequest request) {
        getRequired(id);
        replacePermissions(id, request.getPermissionIds());
    }

    public List<PermissionNode> permissionTree() {
        List<SysPermission> permissions = permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
            .eq(SysPermission::getStatus, "ENABLED")
            .orderByAsc(SysPermission::getSortOrder)
            .orderByAsc(SysPermission::getId));
        Map<Long, PermissionNode> nodeMap = new LinkedHashMap<>();
        permissions.forEach(permission -> nodeMap.put(permission.getId(), PermissionNode.from(permission)));
        List<PermissionNode> roots = new ArrayList<>();
        nodeMap.values().forEach(node -> {
            if (node.getParentId() == null || !nodeMap.containsKey(node.getParentId())) {
                roots.add(node);
            } else {
                nodeMap.get(node.getParentId()).getChildren().add(node);
            }
        });
        roots.sort(Comparator.comparing(PermissionNode::getSortOrder, Comparator.nullsLast(Integer::compareTo)));
        return roots;
    }

    private SysRole getRequired(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return role;
    }

    private List<Long> permissionIds(Long roleId) {
        return rolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId))
            .stream()
            .map(SysRolePermission::getPermissionId)
            .toList();
    }

    private void replacePermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        permissionIds.stream().distinct().forEach(permissionId -> {
            SysRolePermission relation = new SysRolePermission();
            relation.setRoleId(roleId);
            relation.setPermissionId(permissionId);
            rolePermissionMapper.insert(relation);
        });
    }

    private void apply(SysRole role, RoleRequest request) {
        role.setCampusId(request.getCampusId());
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setScopeType(request.getScopeType());
        role.setDataScope(request.getDataScope());
        role.setStatus(request.getStatus());
        role.setRemark(request.getRemark());
    }
}
