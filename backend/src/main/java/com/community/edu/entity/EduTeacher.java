package com.community.edu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("edu_teacher")
public class EduTeacher extends CampusEntity {

    private Long userId;
    private String employeeNo;
    private String name;
    private String gender;
    private String phone;
    private String title;
    private String specialties;
    private String intro;
    private LocalDate hireDate;
    private String status;
}
