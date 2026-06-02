package com.community.edu.admin.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddClassStudentRequest {

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    private LocalDate joinDate;
}