package com.community.edu.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

public class ClassScheduleResponses {

    @Getter
    @Setter
    public static class ScheduleItem {
        private Long id;
        private Long classId;
        private String className;
        private Long courseId;
        private String courseName;
        private Long teacherId;
        private String teacherName;
        private Integer lessonNo;
        private LocalDate lessonDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String topic;
        private String content;
        private BigDecimal lessonHours;
        private String status;
        private String classroom;
        private String onlineUrl;
    }
}
