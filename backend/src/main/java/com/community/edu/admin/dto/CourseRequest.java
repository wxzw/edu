package com.community.edu.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseRequest {

    @NotBlank(message = "课程编码不能为空")
    private String courseCode;

    @NotBlank(message = "课程体系不能为空")
    private String courseSystem;

    @NotBlank(message = "课程名称不能为空")
    private String name;

    private String levelName;
    private BigDecimal targetAgeMin;
    private BigDecimal targetAgeMax;
    private String gradeScope;

    @NotNull(message = "包含课时不能为空")
    private BigDecimal totalHours;

    @NotNull(message = "课时单价不能为空")
    private BigDecimal unitPrice;

    @NotNull(message = "套餐价格不能为空")
    private BigDecimal packagePrice;

    private String description;
    private String status;
}
