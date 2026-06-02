package com.community.edu.admin.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleRequest {

    private Long campusId;

    @NotBlank(message = "角色编码不能为空")
    private String code;

    @NotBlank(message = "角色名称不能为空")
    private String name;

    private String scopeType;
    private String dataScope;
    private String status;
    private String remark;
    private List<Long> permissionIds;
}
