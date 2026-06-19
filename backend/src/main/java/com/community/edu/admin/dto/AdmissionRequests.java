package com.community.edu.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class AdmissionRequests {

    @Getter
    @Setter
    public static class ConfirmRegistrationRequest {
        private Long studentId;
        private String studentNo;
        private String studentName;
        private String nickname;
        private String gender;
        private LocalDate birthday;
        private String grade;
        private String school;
        private String englishLevel;
        private String learningGoal;
        private String relation;

        @NotNull
        private Long classId;
        private LocalDate joinDate;
        private String remark;
    }

    @Getter
    @Setter
    public static class RejectRegistrationRequest {
        @NotBlank
        private String reviewRemark;
    }

    @Getter
    @Setter
    public static class BatchEnrollmentRequest {
        @Valid
        @NotEmpty
        private List<BatchEnrollmentRow> rows = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class BatchEnrollmentRow {
        @NotNull
        private Long courseId;
        @NotNull
        private Long classId;
        @NotBlank
        private String guardianPhone;
        private String guardianName;
        @NotBlank
        private String studentName;
        private String studentNo;
        private String gender;
        private LocalDate birthday;
        private String grade;
        private String school;
        private String englishLevel;
        private String learningGoal;
        private BigDecimal purchasedHours;
        private LocalDate joinDate;
    }
}
