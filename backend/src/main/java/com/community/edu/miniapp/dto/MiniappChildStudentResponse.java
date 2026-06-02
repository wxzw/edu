package com.community.edu.miniapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MiniappChildStudentResponse {

    private Long studentId;
    private Long campusId;
    private String name;
    private String nickname;
    private String avatarUrl;
    private String grade;
    private String school;
    private String relation;
}
