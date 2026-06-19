package com.community.edu.course.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class PublicCourseResponses {

    @Getter
    @Setter
    public static class CampusItem {
        private Long id;
        private String code;
        private String name;
        private String shortName;
        private String address;
        private String businessHours;
    }

    @Getter
    @Setter
    public static class CourseSummary {
        private Long id;
        private Long campusId;
        private String campusName;
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
        private String publicSummary;
        private String coverUrl;
        private String publicStatus;
        private String teacherNames;
        private Integer openClassCount;
    }

    @Getter
    @Setter
    public static class CourseDetail extends CourseSummary {
        private String publicDetail;
        private List<TeacherBrief> teachers = new ArrayList<>();
        private List<ClassOption> classes = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class TeacherBrief {
        private Long id;
        private String name;
        private String avatarUrl;
        private String title;
        private String specialties;
        private String intro;
        private String roleName;
    }

    @Getter
    @Setter
    public static class ClassOption {
        private Long id;
        private String name;
        private Long headTeacherId;
        private String headTeacherName;
        private String classroom;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer maxStudents;
        private Integer currentStudents;
        private String status;
    }
}
