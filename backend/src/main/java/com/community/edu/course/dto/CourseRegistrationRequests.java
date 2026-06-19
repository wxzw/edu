package com.community.edu.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

public class CourseRegistrationRequests {

    @Getter
    @Setter
    public static class CreateRegistrationRequest {
        @NotNull
        private Long courseId;
        private Long preferredClassId;
        private String applicantName;
        @NotBlank
        private String childName;
        private BigDecimal childAge;
        private String childGrade;
        private String note;
    }
}
