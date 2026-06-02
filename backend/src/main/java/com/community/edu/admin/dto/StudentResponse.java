package com.community.edu.admin.dto;

import com.community.edu.entity.EduStudent;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentResponse {

    private Long id;
    private Long campusId;
    private Long userId;
    private String studentNo;
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

    public static StudentResponse from(EduStudent student) {
        return StudentResponse.builder()
            .id(student.getId())
            .campusId(student.getCampusId())
            .userId(student.getUserId())
            .studentNo(student.getStudentNo())
            .name(student.getName())
            .nickname(student.getNickname())
            .avatarUrl(student.getAvatarUrl())
            .gender(student.getGender())
            .birthday(student.getBirthday())
            .grade(student.getGrade())
            .school(student.getSchool())
            .englishLevel(student.getEnglishLevel())
            .learningGoal(student.getLearningGoal())
            .status(student.getStatus())
            .enrolledAt(student.getEnrolledAt())
            .build();
    }
}