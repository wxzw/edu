package com.community.edu.admin.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentRequest {

    private Long userId;

    @NotBlank(message = "学号不能为空")
    private String studentNo;

    @NotBlank(message = "学生姓名不能为空")
    private String name;

    private String nickname;
    private String avatarUrl;
    private String gender;
    private LocalDate birthday;
    private String grade;
    private String school;
    private String englishLevel;
    private String learningGoal;
    private String status;
    private LocalDate enrolledAt;
}