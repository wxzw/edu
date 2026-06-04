package com.community.edu.student.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class StudentP1Responses {

    @Getter
    @Setter
    public static class MaterialCategory {
        private Long id;
        private Long parentId;
        private String name;
        private Integer sortOrder;
    }

    @Getter
    @Setter
    public static class MaterialSummary {
        private Long id;
        private Long categoryId;
        private String categoryName;
        private String title;
        private String description;
        private String resourceType;
        private String studyType;
        private Boolean allowDownload;
        private Long fileId;
        private String fileName;
        private String fileUrl;
        private String contentType;
        private Long fileSize;
        private String coverUrl;
        private String previewPath;
        private String downloadPath;
        private OffsetDateTime updatedAt;
    }

    @Getter
    @Setter
    public static class ActivitySummary {
        private Long id;
        private String title;
        private String description;
        private String coverUrl;
        private OffsetDateTime startTime;
        private OffsetDateTime endTime;
        private String location;
        private BigDecimal fee;
        private Integer quota;
        private Integer registeredCount;
        private String status;
        private String activityStatus;
        private Long registrationId;
        private String registrationStatus;
        private Long orderId;
        private String payStatus;
    }

    @Getter
    @Setter
    public static class ActivityDetail extends ActivitySummary {
    }

    @Getter
    @Setter
    public static class JoinActivityResponse {
        private Long activityId;
        private Long registrationId;
        private Long orderId;
        private Boolean payRequired;
        private BigDecimal amount;
        private String status;
        private String message;
    }

    @Getter
    @Setter
    public static class RegistrationItem {
        private Long id;
        private Long activityId;
        private Long orderId;
        private String registrationNo;
        private BigDecimal amount;
        private String status;
        private OffsetDateTime registeredAt;
        private String activityTitle;
        private OffsetDateTime startTime;
        private OffsetDateTime endTime;
        private String location;
        private String payStatus;
    }

    @Getter
    @Setter
    public static class OrderDetail {
        private Long id;
        private String orderNo;
        private Long activityId;
        private String activityTitle;
        private BigDecimal totalAmount;
        private BigDecimal discountAmount;
        private BigDecimal paidAmount;
        private String payStatus;
        private String payChannel;
        private String transactionNo;
        private OffsetDateTime payTime;
        private String status;
        private String remark;
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    public static class MockPayResponse {
        private Long orderId;
        private Long registrationId;
        private String payStatus;
        private String transactionNo;
        private BigDecimal paidAmount;
    }

    @Getter
    @Setter
    public static class NotificationItem {
        private Long id;
        private String bizType;
        private Long bizId;
        private String title;
        private String content;
        private String status;
        private OffsetDateTime readAt;
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    public static class NotificationPage {
        private List<NotificationItem> records;
        private Long unreadCount;
    }
}
