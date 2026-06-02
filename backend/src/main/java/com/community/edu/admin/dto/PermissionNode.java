package com.community.edu.admin.dto;

import com.community.edu.entity.SysPermission;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PermissionNode {

    private Long id;
    private Long parentId;
    private String code;
    private String name;
    private String permissionType;
    private String routePath;
    private String icon;
    private Integer sortOrder;
    @Builder.Default
    private List<PermissionNode> children = new ArrayList<>();

    public static PermissionNode from(SysPermission permission) {
        return PermissionNode.builder()
            .id(permission.getId())
            .parentId(permission.getParentId())
            .code(permission.getCode())
            .name(permission.getName())
            .permissionType(permission.getPermissionType())
            .routePath(permission.getRoutePath())
            .icon(permission.getIcon())
            .sortOrder(permission.getSortOrder())
            .children(new ArrayList<>())
            .build();
    }
}
