package com.community.edu.admin.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassStudentResponse {

    private Long classStudentId;
    private Long studentId;
    private String studentNo;
    private String name;
    private String nickname;
    private String gender;
    private LocalDate birthday;
    private String grade;
    private String school;
    private String englishLevel;
    private String studentStatus;
    private LocalDate joinDate;
    private LocalDate leaveDate;
    private String classStudentStatus;
}