package com.community.edu.admin.dto;

import com.community.edu.entity.SysUser;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String accountType;
    private String status;
    private List<Long> roleIds;
    private List<Long> campusIds;
    private OffsetDateTime lastLoginAt;
    private OffsetDateTime createdAt;

    public static UserResponse from(SysUser user, List<Long> roleIds, List<Long> campusIds) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .phone(user.getPhone())
            .email(user.getEmail())
            .accountType(user.getAccountType())
            .status(user.getStatus())
            .roleIds(roleIds)
            .campusIds(campusIds)
            .lastLoginAt(user.getLastLoginAt())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
