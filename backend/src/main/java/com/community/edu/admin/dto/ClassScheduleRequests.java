package com.community.edu.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class ClassScheduleRequests {

    @Getter
    @Setter
    public static class GenerateSchedulesRequest {
        private Long teacherId;
        @NotNull
        private LocalDate startDate;
        private LocalDate endDate;
        @NotEmpty
        private List<Integer> weekdays;
        @NotNull
        private LocalTime startTime;
        @NotNull
        private LocalTime endTime;
        private Integer totalLessons;
        private BigDecimal lessonHours;
        private String classroom;
        private String topicPrefix;
        private String content;
    }

    @Getter
    @Setter
    public static class UpdateScheduleRequest {
        private Long teacherId;
        private LocalDate lessonDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String topic;
        private String content;
        private BigDecimal lessonHours;
        private String classroom;
        private String onlineUrl;
        private String status;
    }
}
