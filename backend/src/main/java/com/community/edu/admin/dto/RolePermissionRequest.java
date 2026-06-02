package com.community.edu.admin.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolePermissionRequest {

    @NotNull(message = "权限列表不能为空")
    private List<Long> permissionIds;
}
