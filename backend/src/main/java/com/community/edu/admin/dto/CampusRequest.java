package com.community.edu.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CampusRequest {

    @NotBlank(message = "校区编码不能为空")
    @Size(max = 32, message = "校区编码不能超过32个字符")
    private String code;

    @NotBlank(message = "校区名称不能为空")
    @Size(max = 100, message = "校区名称不能超过100个字符")
    private String name;

    private String shortName;
    private String contactName;
    private String contactPhone;
    private String address;
    private String businessHours;
    private String status;
}
