package com.community.edu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("edu_guardian")
public class EduGuardian extends CampusEntity {

    private Long userId;
    private String name;
    private String phone;
    private String wxOpenId;
    private String wxUnionId;
    private String status;
}
