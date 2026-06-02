package com.community.edu.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassRequest {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotBlank(message = "班级编号不能为空")
    private String classNo;

    @NotBlank(message = "班级名称不能为空")
    private String name;

    private Long headTeacherId;
    private String classroom;
    private String classWechatQrUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxStudents;
    private String status;
    private String remark;
}
