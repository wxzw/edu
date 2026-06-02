package com.community.edu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private Long campusId;
    private String code;
    private String name;
    private String scopeType;
    private String dataScope;
    private String status;
    private String remark;
}
