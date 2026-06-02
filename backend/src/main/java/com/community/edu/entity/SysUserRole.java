package com.community.edu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_user_role")
public class SysUserRole extends BaseEntity {

    private Long userId;
    private Long roleId;
    private Long campusId;
}
