package com.community.edu.admin;

import com.community.edu.admin.dto.AdminP1Requests;
import com.community.edu.admin.dto.AdminP1Responses;
import com.community.edu.admin.dto.AdminP1Rows;
import com.community.edu.common.context.CurrentUserHolder;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.common.response.PageResponse;
import com.community.edu.common.util.StringUtil;
import com.community.edu.mapper.AdminP1Mapper;
import com.community.edu.service.CampusScopeService;
import com.community.edu.service.LocalFileStorageService;
import com.community.edu.service.LocalFileStorageService.StoredFile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 运营管理服务（P1）。处理资料库、活动、通知等运营业务。
 */
@Service
@RequiredArgsConstructor
public class AdminP1Service {

    private static final String ENABLED = "ENABLED";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String DRAFT = "DRAFT";

    private final CampusScopeService campusScopeService;
    private final AdminP1Mapper mapper;
    private final LocalFileStorageService fileStorageService;

    @Transactional
    public AdminP1Responses.FileInfo uploadLocalFile(MultipartFile file, String bizType) {
        Long campusId = campusScopeService.requiredCampusId();
        Long operatorId = CurrentUserHolder.getRequired().getUserId();
        StoredFile storedFile = fileStorageService.store(file, campusId);
        Long fileId = mapper.insertLocalFile(
            campusId,
            storedFile.objectKey(),
            "/api/admin/files/local/" + storedFile.objectKey(),
            storedFile.fileName(),
            storedFile.contentType(),
            storedFile.fileSize(),
            operatorId,
            StringUtils.hasText(bizType) ? bizType : "MATERIAL",
            operatorId
        );
        return toFile(requiredFile(campusId, fileId));
    }

    public List<AdminP1Responses.CategoryItem> categories() {
        Long campusId = campusScopeService.requiredCampusId();
        return mapper.selectCategories(campusId).stream().map(this::toCategory).toList();
    }

    @Transactional
    public AdminP1Responses.CategoryItem createCategory(AdminP1Requests.CategoryRequest request) {
        Long campusId = campusScopeService.requiredCampusId();
        Long operatorId = CurrentUserHolder.getRequired().getUserId();
        Long id = mapper.insertCategory(
            campusId,
            request.getParentId(),
            request.getName(),
            request.getSortOrder() == null ? 0 : request.getSortOrder(),
            defaultString(request.getStatus(), ENABLED),
            operatorId
        );
        return categories().stream()
            .filter(item -> id.equals(item.getId()))
            .findFirst()
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "分类不存在"));
    }

    @Transactional
    public AdminP1Responses.CategoryItem updateCategory(Long id, AdminP1Requests.CategoryRequest request) {
        Long campusId = campusScopeService.requiredCampusId();
        mapper.updateCategory(
            campusId,
            id,
            request.getParentId(),
            request.getName(),
            request.getSortOrder() == null ? 0 : request.getSortOrder(),
            defaultString(request.getStatus(), ENABLED),
            CurrentUserHolder.getRequired().getUserId()
        );
        return categories().stream()
            .filter(item -> id.equals(item.getId()))
            .findFirst()
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "分类不存在"));
    }

    public PageResponse<AdminP1Responses.MaterialItem> materials(AdminP1Requests.MaterialQuery query) {
        Long campusId = campusScopeService.requiredCampusId();
        long total = mapper.countMaterials(
            campusId,
            query.getCategoryId(),
            StringUtil.blankToNull(query.getKeyword()),
            StringUtil.blankToNull(query.getResourceType()),
            StringUtil.blankToNull(query.getStudyType()),
            StringUtil.blankToNull(query.getVisibility()),
            StringUtil.blankToNull(query.getStatus()),
            query.getStartDate(),
            query.getEndDate()
        );
        List<AdminP1Responses.MaterialItem> records = mapper.selectMaterials(
                campusId,
                query.getCategoryId(),
                StringUtil.blankToNull(query.getKeyword()),
                StringUtil.blankToNull(query.getResourceType()),
                StringUtil.blankToNull(query.getStudyType()),
                StringUtil.blankToNull(query.getVisibility()),
                StringUtil.blankToNull(query.getStatus()),
                query.getStartDate(),
                query.getEndDate(),
                query.getPageSize(),
                offset(query)
            )
            .stream()
            .map(this::toMaterial)
            .toList();
        return PageResponse.of(records, total, query.getPageNo(), query.getPageSize());
    }

    public AdminP1Responses.MaterialItem material(Long id) {
        Long campusId = campusScopeService.requiredCampusId();
        AdminP1Rows.MaterialRow row = mapper.selectMaterial(campusId, id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "资料不存在");
        }
        return toMaterial(row);
    }

    @Transactional
    public AdminP1Responses.MaterialItem createMaterial(AdminP1Requests.MaterialRequest request) {
        Long campusId = campusScopeService.requiredCampusId();
        Long operatorId = CurrentUserHolder.getRequired().getUserId();
        validateMaterialRequest(request);
        Long id = mapper.insertMaterial(
            campusId,
            request.getCategoryId(),
            request.getTitle(),
            request.getDescription(),
            request.getResourceType(),
            request.getCoverFileId(),
            request.getFileId(),
            request.getOwnerTeacherId(),
            defaultString(request.getVisibility(), "CAMPUS"),
            defaultString(request.getStudyType(), "OPTIONAL"),
            Boolean.TRUE.equals(request.getAllowDownload()),
            defaultString(request.getStatus(), DRAFT),
            operatorId
        );
        replaceMaterialClasses(campusId, id, request.getClassIds(), operatorId);
        return material(id);
    }

    @Transactional
    public AdminP1Responses.MaterialItem updateMaterial(Long id, AdminP1Requests.MaterialRequest request) {
        Long campusId = campusScopeService.requiredCampusId();
        Long operatorId = CurrentUserHolder.getRequired().getUserId();
        validateMaterialRequest(request);
        mapper.updateMaterial(
            campusId,
            id,
            request.getCategoryId(),
            request.getTitle(),
            request.getDescription(),
            request.getResourceType(),
            request.getCoverFileId(),
            request.getFileId(),
            request.getOwnerTeacherId(),
            defaultString(request.getVisibility(), "CAMPUS"),
            defaultString(request.getStudyType(), "OPTIONAL"),
            Boolean.TRUE.equals(request.getAllowDownload()),
            defaultString(request.getStatus(), DRAFT),
            operatorId
        );
        replaceMaterialClasses(campusId, id, request.getClassIds(), operatorId);
        return material(id);
    }

    @Transactional
    public void updateMaterialStatus(Long id, String status) {
        mapper.updateMaterialStatus(
            campusScopeService.requiredCampusId(),
            id,
            defaultString(status, DRAFT),
            CurrentUserHolder.getRequired().getUserId()
        );
    }

    public PageResponse<AdminP1Responses.ActivityItem> activities(AdminP1Requests.ActivityQuery query) {
        Long campusId = campusScopeService.requiredCampusId();
        long total = mapper.countActivities(campusId, StringUtil.blankToNull(query.getKeyword()), StringUtil.blankToNull(query.getStatus()), query.getStartDate(), query.getEndDate());
        List<AdminP1Responses.ActivityItem> records = mapper.selectActivities(
                campusId,
                StringUtil.blankToNull(query.getKeyword()),
                StringUtil.blankToNull(query.getStatus()),
                query.getStartDate(),
                query.getEndDate(),
                query.getPageSize(),
                offset(query)
            )
            .stream()
            .map(this::toActivity)
            .toList();
        return PageResponse.of(records, total, query.getPageNo(), query.getPageSize());
    }

    public AdminP1Responses.ActivityItem activity(Long id) {
        Long campusId = campusScopeService.requiredCampusId();
        AdminP1Rows.ActivityRow row = mapper.selectActivity(campusId, id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "活动不存在");
        }
        return toActivity(row);
    }

    @Transactional
    public AdminP1Responses.ActivityItem createActivity(AdminP1Requests.ActivityRequest request) {
        validateActivityRequest(request);
        Long id = mapper.insertActivity(
            campusScopeService.requiredCampusId(),
            request.getTitle(),
            request.getDescription(),
            request.getCoverFileId(),
            request.getStartTime(),
            request.getEndTime(),
            request.getLocation(),
            request.getFee() == null ? BigDecimal.ZERO : request.getFee(),
            request.getQuota(),
            defaultString(request.getStatus(), DRAFT),
            CurrentUserHolder.getRequired().getUserId()
        );
        return activity(id);
    }

    @Transactional
    public AdminP1Responses.ActivityItem updateActivity(Long id, AdminP1Requests.ActivityRequest request) {
        validateActivityRequest(request);
        mapper.updateActivity(
            campusScopeService.requiredCampusId(),
            id,
            request.getTitle(),
            request.getDescription(),
            request.getCoverFileId(),
            request.getStartTime(),
            request.getEndTime(),
            request.getLocation(),
            request.getFee() == null ? BigDecimal.ZERO : request.getFee(),
            request.getQuota(),
            defaultString(request.getStatus(), DRAFT),
            CurrentUserHolder.getRequired().getUserId()
        );
        return activity(id);
    }

    @Transactional
    public void updateActivityStatus(Long id, String status) {
        mapper.updateActivityStatus(
            campusScopeService.requiredCampusId(),
            id,
            defaultString(status, DRAFT),
            CurrentUserHolder.getRequired().getUserId()
        );
    }

    public PageResponse<AdminP1Responses.RegistrationItem> registrations(AdminP1Requests.RegistrationQuery query) {
        Long campusId = campusScopeService.requiredCampusId();
        long total = mapper.countRegistrations(campusId, query.getActivityId(), StringUtil.blankToNull(query.getStatus()), query.getStartDate(), query.getEndDate());
        List<AdminP1Responses.RegistrationItem> records = mapper.selectRegistrations(
                campusId,
                query.getActivityId(),
                StringUtil.blankToNull(query.getStatus()),
                query.getStartDate(),
                query.getEndDate(),
                query.getPageSize(),
                offset(query)
            )
            .stream()
            .map(this::toRegistration)
            .toList();
        return PageResponse.of(records, total, query.getPageNo(), query.getPageSize());
    }

    public PageResponse<AdminP1Responses.OrderItem> orders(AdminP1Requests.OrderQuery query) {
        Long campusId = campusScopeService.requiredCampusId();
        long total = mapper.countOrders(
            campusId,
            StringUtil.blankToNull(query.getOrderType()),
            StringUtil.blankToNull(query.getPayStatus()),
            query.getStudentId(),
            query.getStartDate(),
            query.getEndDate()
        );
        List<AdminP1Responses.OrderItem> records = mapper.selectOrders(
                campusId,
                StringUtil.blankToNull(query.getOrderType()),
                StringUtil.blankToNull(query.getPayStatus()),
                query.getStudentId(),
                query.getStartDate(),
                query.getEndDate(),
                query.getPageSize(),
                offset(query)
            )
            .stream()
            .map(this::toOrder)
            .toList();
        return PageResponse.of(records, total, query.getPageNo(), query.getPageSize());
    }

    public PageResponse<AdminP1Responses.PaymentItem> payments(AdminP1Requests.PaymentQuery query) {
        Long campusId = campusScopeService.requiredCampusId();
        long total = mapper.countPayments(campusId, StringUtil.blankToNull(query.getStatus()), query.getOrderId(), query.getStartDate(), query.getEndDate());
        List<AdminP1Responses.PaymentItem> records = mapper.selectPayments(
                campusId,
                StringUtil.blankToNull(query.getStatus()),
                query.getOrderId(),
                query.getStartDate(),
                query.getEndDate(),
                query.getPageSize(),
                offset(query)
            )
            .stream()
            .map(this::toPayment)
            .toList();
        return PageResponse.of(records, total, query.getPageNo(), query.getPageSize());
    }

    public PageResponse<AdminP1Responses.NotificationItem> notifications(AdminP1Requests.NotificationQuery query) {
        Long campusId = campusScopeService.requiredCampusId();
        long total = mapper.countNotifications(
            campusId,
            StringUtil.blankToNull(query.getStatus()),
            StringUtil.blankToNull(query.getBizType()),
            query.getReceiverStudentId(),
            query.getStartDate(),
            query.getEndDate()
        );
        List<AdminP1Responses.NotificationItem> records = mapper.selectNotifications(
                campusId,
                StringUtil.blankToNull(query.getStatus()),
                StringUtil.blankToNull(query.getBizType()),
                query.getReceiverStudentId(),
                query.getStartDate(),
                query.getEndDate(),
                query.getPageSize(),
                offset(query)
            )
            .stream()
            .map(this::toNotification)
            .toList();
        return PageResponse.of(records, total, query.getPageNo(), query.getPageSize());
    }

    @Transactional
    public AdminP1Responses.NotificationPublishResult publishNotification(AdminP1Requests.NotificationRequest request) {
        Long campusId = campusScopeService.requiredCampusId();
        Long operatorId = CurrentUserHolder.getRequired().getUserId();
        List<Long> studentIds = resolveNotificationTargets(campusId, request);
        for (Long studentId : studentIds) {
            mapper.insertNotification(
                campusId,
                studentId,
                defaultString(request.getBizType(), "ADMIN_NOTICE"),
                request.getBizId(),
                request.getTitle(),
                request.getContent(),
                operatorId
            );
        }
        AdminP1Responses.NotificationPublishResult result = new AdminP1Responses.NotificationPublishResult();
        result.setSentCount(studentIds.size());
        return result;
    }

    private void validateMaterialRequest(AdminP1Requests.MaterialRequest request) {
        String visibility = defaultString(request.getVisibility(), "CAMPUS");
        if ("CLASS".equals(visibility) && (request.getClassIds() == null || request.getClassIds().isEmpty())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "班级可见资料必须选择班级");
        }
    }

    private void validateActivityRequest(AdminP1Requests.ActivityRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "结束时间不能早于开始时间");
        }
        if (request.getQuota() != null && request.getQuota() <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "名额必须大于0");
        }
    }

    private void replaceMaterialClasses(Long campusId, Long materialId, List<Long> classIds, Long operatorId) {
        mapper.clearMaterialClasses(campusId, materialId, operatorId);
        if (classIds == null) {
            return;
        }
        for (Long classId : classIds.stream().distinct().toList()) {
            mapper.insertMaterialClass(campusId, materialId, classId, operatorId);
        }
    }

    private List<Long> resolveNotificationTargets(Long campusId, AdminP1Requests.NotificationRequest request) {
        String targetType = request.getTargetType();
        List<AdminP1Rows.StudentTargetRow> rows;
        if ("CAMPUS".equals(targetType)) {
            rows = mapper.selectCampusStudents(campusId);
        } else if ("CLASS".equals(targetType)) {
            if (request.getClassId() == null) {
                throw new BizException(ErrorCode.BAD_REQUEST, "请选择班级");
            }
            rows = mapper.selectClassStudents(campusId, request.getClassId());
        } else if ("STUDENT".equals(targetType)) {
            if (request.getStudentId() == null) {
                throw new BizException(ErrorCode.BAD_REQUEST, "请选择学生");
            }
            AdminP1Rows.StudentTargetRow row = mapper.selectStudentTarget(campusId, request.getStudentId());
            rows = row == null ? List.of() : List.of(row);
        } else {
            throw new BizException(ErrorCode.BAD_REQUEST, "发送范围不正确");
        }
        List<Long> studentIds = new ArrayList<>();
        for (AdminP1Rows.StudentTargetRow row : rows) {
            studentIds.add(row.getStudentId());
        }
        if (studentIds.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "没有可发送的学生");
        }
        return studentIds;
    }

    private AdminP1Rows.FileRow requiredFile(Long campusId, Long fileId) {
        AdminP1Rows.FileRow row = mapper.selectFile(campusId, fileId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        return row;
    }

    private AdminP1Responses.FileInfo toFile(AdminP1Rows.FileRow row) {
        AdminP1Responses.FileInfo response = new AdminP1Responses.FileInfo();
        response.setId(row.getId());
        response.setStorageType(row.getStorageType());
        response.setObjectKey(row.getObjectKey());
        response.setUrl(row.getUrl());
        response.setFileName(row.getFileName());
        response.setContentType(row.getContentType());
        response.setFileSize(row.getFileSize());
        response.setBizType(row.getBizType());
        response.setStatus(row.getStatus());
        response.setCreatedAt(row.getCreatedAt());
        return response;
    }

    private AdminP1Responses.CategoryItem toCategory(AdminP1Rows.CategoryRow row) {
        AdminP1Responses.CategoryItem response = new AdminP1Responses.CategoryItem();
        response.setId(row.getId());
        response.setParentId(row.getParentId());
        response.setCategoryType(row.getCategoryType());
        response.setName(row.getName());
        response.setSortOrder(row.getSortOrder());
        response.setStatus(row.getStatus());
        return response;
    }

    private AdminP1Responses.MaterialItem toMaterial(AdminP1Rows.MaterialRow row) {
        AdminP1Responses.MaterialItem response = new AdminP1Responses.MaterialItem();
        response.setId(row.getId());
        response.setCategoryId(row.getCategoryId());
        response.setCategoryName(row.getCategoryName());
        response.setTitle(row.getTitle());
        response.setDescription(row.getDescription());
        response.setResourceType(row.getResourceType());
        response.setCoverFileId(row.getCoverFileId());
        response.setCoverUrl(row.getCoverUrl());
        response.setFileId(row.getFileId());
        response.setFileName(row.getFileName());
        response.setOwnerTeacherId(row.getOwnerTeacherId());
        response.setOwnerTeacherName(row.getOwnerTeacherName());
        response.setVisibility(row.getVisibility());
        response.setStudyType(row.getStudyType());
        response.setAllowDownload(row.getAllowDownload());
        response.setAuditStatus(row.getAuditStatus());
        response.setStatus(row.getStatus());
        response.setClassIds(parseClassIds(row.getClassIdsCsv()));
        response.setCreatedAt(row.getCreatedAt());
        response.setUpdatedAt(row.getUpdatedAt());
        return response;
    }

    private AdminP1Responses.ActivityItem toActivity(AdminP1Rows.ActivityRow row) {
        AdminP1Responses.ActivityItem response = new AdminP1Responses.ActivityItem();
        response.setId(row.getId());
        response.setTitle(row.getTitle());
        response.setDescription(row.getDescription());
        response.setCoverFileId(row.getCoverFileId());
        response.setCoverUrl(row.getCoverUrl());
        response.setStartTime(row.getStartTime());
        response.setEndTime(row.getEndTime());
        response.setLocation(row.getLocation());
        response.setFee(row.getFee());
        response.setQuota(row.getQuota());
        response.setRegisteredCount(row.getRegisteredCount());
        response.setStatus(row.getStatus());
        response.setPublishedAt(row.getPublishedAt());
        response.setCreatedAt(row.getCreatedAt());
        response.setUpdatedAt(row.getUpdatedAt());
        return response;
    }

    private AdminP1Responses.RegistrationItem toRegistration(AdminP1Rows.RegistrationRow row) {
        AdminP1Responses.RegistrationItem response = new AdminP1Responses.RegistrationItem();
        response.setId(row.getId());
        response.setActivityId(row.getActivityId());
        response.setActivityTitle(row.getActivityTitle());
        response.setStudentId(row.getStudentId());
        response.setStudentName(row.getStudentName());
        response.setGuardianId(row.getGuardianId());
        response.setOrderId(row.getOrderId());
        response.setRegistrationNo(row.getRegistrationNo());
        response.setAmount(row.getAmount());
        response.setStatus(row.getStatus());
        response.setRegisteredAt(row.getRegisteredAt());
        response.setPayStatus(row.getPayStatus());
        return response;
    }

    private AdminP1Responses.OrderItem toOrder(AdminP1Rows.OrderRow row) {
        AdminP1Responses.OrderItem response = new AdminP1Responses.OrderItem();
        response.setId(row.getId());
        response.setOrderNo(row.getOrderNo());
        response.setOrderType(row.getOrderType());
        response.setStudentId(row.getStudentId());
        response.setStudentName(row.getStudentName());
        response.setGuardianId(row.getGuardianId());
        response.setActivityId(row.getActivityId());
        response.setActivityTitle(row.getActivityTitle());
        response.setTotalAmount(row.getTotalAmount());
        response.setDiscountAmount(row.getDiscountAmount());
        response.setPaidAmount(row.getPaidAmount());
        response.setPayStatus(row.getPayStatus());
        response.setPayChannel(row.getPayChannel());
        response.setTransactionNo(row.getTransactionNo());
        response.setPayTime(row.getPayTime());
        response.setStatus(row.getStatus());
        response.setCreatedAt(row.getCreatedAt());
        return response;
    }

    private AdminP1Responses.PaymentItem toPayment(AdminP1Rows.PaymentRow row) {
        AdminP1Responses.PaymentItem response = new AdminP1Responses.PaymentItem();
        response.setId(row.getId());
        response.setOrderId(row.getOrderId());
        response.setOrderNo(row.getOrderNo());
        response.setPaymentNo(row.getPaymentNo());
        response.setPayChannel(row.getPayChannel());
        response.setAmount(row.getAmount());
        response.setTransactionNo(row.getTransactionNo());
        response.setStatus(row.getStatus());
        response.setPaidAt(row.getPaidAt());
        response.setCreatedAt(row.getCreatedAt());
        return response;
    }

    private AdminP1Responses.NotificationItem toNotification(AdminP1Rows.NotificationRow row) {
        AdminP1Responses.NotificationItem response = new AdminP1Responses.NotificationItem();
        response.setId(row.getId());
        response.setReceiverUserId(row.getReceiverUserId());
        response.setReceiverStudentId(row.getReceiverStudentId());
        response.setReceiverStudentName(row.getReceiverStudentName());
        response.setBizType(row.getBizType());
        response.setBizId(row.getBizId());
        response.setTitle(row.getTitle());
        response.setContent(row.getContent());
        response.setStatus(row.getStatus());
        response.setReadAt(row.getReadAt());
        response.setCreatedAt(row.getCreatedAt());
        return response;
    }

    private List<Long> parseClassIds(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (String item : value.split(",")) {
            if (StringUtils.hasText(item)) {
                ids.add(Long.valueOf(item));
            }
        }
        return ids;
    }



    private String defaultString(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private long offset(com.community.edu.common.dto.PageQuery query) {
        return (query.getPageNo() - 1) * query.getPageSize();
    }
}
