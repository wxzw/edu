package com.community.edu.admin.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class AdmissionResponses {

    @Getter
    @Setter
    public static class RegistrationItem {
        private Long id;
        private String registrationNo;
        private Long courseId;
        private String courseName;
        private Long studentId;
        private String studentName;
        private Long guardianId;
        private String guardianName;
        private String applicantPhone;
        private String childName;
        private String childGrade;
        private BigDecimal childAge;
        private Long orderId;
        private String orderNo;
        private String payStatus;
        private BigDecimal amount;
        private String status;
        private Long preferredClassId;
        private String preferredClassName;
        private Long assignedClassId;
        private String assignedClassName;
        private String note;
        private String reviewRemark;
        private OffsetDateTime registeredAt;
        private OffsetDateTime paidAt;
        private OffsetDateTime confirmedAt;
    }

    @Getter
    @Setter
    public static class BatchEnrollmentResult {
        private Integer successCount;
        private Integer failedCount;
        private List<RowResult> results = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class RowResult {
        private Integer rowIndex;
        private Boolean success;
        private Long studentId;
        private String studentName;
        private String message;
    }
}
