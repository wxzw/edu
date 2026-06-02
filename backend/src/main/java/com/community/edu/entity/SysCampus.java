package com.community.edu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_campus")
public class SysCampus extends BaseEntity {

    private String code;
    private String name;
    private String shortName;
    private String contactName;
    private String contactPhone;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String businessHours;
    private String status;
    private String settings;
}
