package com.community.edu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("edu_student")
public class EduStudent extends CampusEntity {

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
}
