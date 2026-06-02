package com.community.edu.admin.dto;

import com.community.edu.entity.SysRole;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoleResponse {

    private Long id;
    private Long campusId;
    private String code;
    private String name;
    private String scopeType;
    private String dataScope;
    private String status;
    private String remark;
    private List<Long> permissionIds;

    public static RoleResponse from(SysRole role, List<Long> permissionIds) {
        return RoleResponse.builder()
            .id(role.getId())
            .campusId(role.getCampusId())
            .code(role.getCode())
            .name(role.getName())
            .scopeType(role.getScopeType())
            .dataScope(role.getDataScope())
            .status(role.getStatus())
            .remark(role.getRemark())
            .permissionIds(permissionIds)
            .build();
    }
}
