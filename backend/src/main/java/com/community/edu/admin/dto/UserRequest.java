package com.community.edu.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    private String phone;
    private String email;

    @NotBlank(message = "账号类型不能为空")
    private String accountType;

    private String status;

    @NotEmpty(message = "至少分配一个角色")
    private List<Long> roleIds;

    private List<Long> campusIds;
    private Long defaultCampusId;
}
