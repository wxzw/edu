package com.community.edu.admin.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherRequest {

    private Long userId;

    @NotBlank(message = "工号不能为空")
    private String employeeNo;

    @NotBlank(message = "老师姓名不能为空")
    private String name;

    private String gender;
    private String phone;
    private String title;
    private String intro;
    private LocalDate hireDate;
    private String status;
}
