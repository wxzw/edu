package com.community.edu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_user_campus")
public class SysUserCampus extends BaseEntity {

    private Long userId;
    private Long campusId;
    private String relationType;
    private Boolean isDefault;
}
