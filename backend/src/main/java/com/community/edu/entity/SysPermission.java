package com.community.edu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    private Long parentId;
    private String code;
    private String name;
    private String permissionType;
    private String routePath;
    private String component;
    private String apiMethod;
    private String apiPath;
    private String icon;
    private Integer sortOrder;
    private String status;
}
