package com.community.edu.admin.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class AdminP1Responses {

    @Getter
    @Setter
    public static class FileInfo {
        private Long id;
        private String storageType;
        private String objectKey;
        private String url;
        private String fileName;
        private String contentType;
        private Long fileSize;
        private String bizType;
        private String status;
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    public static class CategoryItem {
        private Long id;
        private Long parentId;
        private String categoryType;
        private String name;
        private Integer sortOrder;
        private String status;
    }

    @Getter
    @Setter
    public static class MaterialItem {
        private Long id;
        private Long categoryId;
        private String categoryName;
        private String title;
        private String description;
        private String resourceType;
        private Long coverFileId;
        private String coverUrl;
        private Long fileId;
        private String fileName;
        private String contentType;
        private Long fileSize;
        private Long ownerTeacherId;
        private String ownerTeacherName;
        private String visibility;
        private String studyType;
        private Boolean allowDownload;
        private String auditStatus;
        private String status;
        private List<Long> classIds;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    @Getter
    @Setter
    public static class ActivityItem {
        private Long id;
        private String title;
        private String description;
        private Long coverFileId;
        private String coverUrl;
        private OffsetDateTime startTime;
        private OffsetDateTime endTime;
        private String location;
        private BigDecimal fee;
        private Integer quota;
        private Integer registeredCount;
        private String status;
        private OffsetDateTime publishedAt;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    @Getter
    @Setter
    public static class RegistrationItem {
        private Long id;
        private Long activityId;
        private String activityTitle;
        private Long studentId;
        private String studentName;
        private Long guardianId;
        private Long orderId;
        private String registrationNo;
        private BigDecimal amount;
        private String status;
        private OffsetDateTime registeredAt;
        private String payStatus;
    }

    @Getter
    @Setter
    public static class OrderItem {
        private Long id;
        private String orderNo;
        private String orderType;
        private Long studentId;
        private String studentName;
        private Long guardianId;
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
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    public static class PaymentItem {
        private Long id;
        private Long orderId;
        private String orderNo;
        private String paymentNo;
        private String payChannel;
        private BigDecimal amount;
        private String transactionNo;
        private String status;
        private OffsetDateTime paidAt;
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    public static class NotificationItem {
        private Long id;
        private Long receiverUserId;
        private Long receiverStudentId;
        private String receiverStudentName;
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
    public static class NotificationPublishResult {
        private Integer sentCount;
    }
}
