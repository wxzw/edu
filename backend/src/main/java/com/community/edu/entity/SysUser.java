package com.community.edu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;
    private String passwordHash;
    private String realName;
    private String phone;
    private String email;
    private String avatarUrl;
    private String accountType;
    private String wxOpenId;
    private String wxUnionId;
    private String status;
    private OffsetDateTime lastLoginAt;
    private String lastLoginIp;
}
