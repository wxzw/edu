package com.community.edu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("edu_course")
public class EduCourse extends CampusEntity {

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
}
