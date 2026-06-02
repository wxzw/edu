package com.community.edu.admin.dto;

import com.community.edu.entity.EduTeacher;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeacherResponse {

    private Long id;
    private Long campusId;
    private Long userId;
    private String employeeNo;
    private String name;
    private String gender;
    private String phone;
    private String title;
    private String intro;
    private LocalDate hireDate;
    private String status;

    public static TeacherResponse from(EduTeacher teacher) {
        return TeacherResponse.builder()
            .id(teacher.getId())
            .campusId(teacher.getCampusId())
            .userId(teacher.getUserId())
            .employeeNo(teacher.getEmployeeNo())
            .name(teacher.getName())
            .gender(teacher.getGender())
            .phone(teacher.getPhone())
            .title(teacher.getTitle())
            .intro(teacher.getIntro())
            .hireDate(teacher.getHireDate())
            .status(teacher.getStatus())
            .build();
    }
}
