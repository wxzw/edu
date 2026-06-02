package com.community.edu.admin.dto;

import com.community.edu.entity.EduCourse;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseResponse {

    private Long id;
    private Long campusId;
    private String courseCode;
    private String courseSystem;
    private String name;
    private String levelName;
    private BigDecimal targetAgeMin;
    private BigDecimal targetAgeMax;
    private String gradeScope;
    private BigDecimal totalHours;
    private BigDecimal unitPrice;
    private BigDecimal packagePrice;
    private String description;
    private String status;

    public static CourseResponse from(EduCourse course) {
        return CourseResponse.builder()
            .id(course.getId())
            .campusId(course.getCampusId())
            .courseCode(course.getCourseCode())
            .courseSystem(course.getCourseSystem())
            .name(course.getName())
            .levelName(course.getLevelName())
            .targetAgeMin(course.getTargetAgeMin())
            .targetAgeMax(course.getTargetAgeMax())
            .gradeScope(course.getGradeScope())
            .totalHours(course.getTotalHours())
            .unitPrice(course.getUnitPrice())
            .packagePrice(course.getPackagePrice())
            .description(course.getDescription())
            .status(course.getStatus())
            .build();
    }
}
