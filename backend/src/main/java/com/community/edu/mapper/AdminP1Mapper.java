package com.community.edu.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.community.edu.admin.dto.AdminP1Rows;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface AdminP1Mapper {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO res_file (
            campus_id, storage_type, bucket_name, object_key, url, file_name, content_type, file_size,
            uploader_id, biz_type, status, created_by, updated_by
        ) VALUES (
            #{campusId}, 'LOCAL', NULL, #{objectKey}, #{url}, #{fileName}, #{contentType}, #{fileSize},
            #{uploaderId}, #{bizType}, 'AVAILABLE', #{operatorId}, #{operatorId}
        )
        RETURNING id
        """)
    Long insertLocalFile(
        @Param("campusId") Long campusId,
        @Param("objectKey") String objectKey,
        @Param("url") String url,
        @Param("fileName") String fileName,
        @Param("contentType") String contentType,
        @Param("fileSize") Long fileSize,
        @Param("uploaderId") Long uploaderId,
        @Param("bizType") String bizType,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT id,
               storage_type,
               object_key,
               url,
               file_name,
               content_type,
               file_size,
               biz_type,
               status,
               created_at
        FROM res_file
        WHERE campus_id = #{campusId}
          AND id = #{fileId}
          AND deleted = 0
        LIMIT 1
        """)
    AdminP1Rows.FileRow selectFile(@Param("campusId") Long campusId, @Param("fileId") Long fileId);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT id,
               parent_id,
               category_type,
               name,
               sort_order,
               status
        FROM res_category
        WHERE campus_id = #{campusId}
          AND deleted = 0
          AND category_type = 'MATERIAL'
        ORDER BY sort_order, id
        """)
    List<AdminP1Rows.CategoryRow> selectCategories(@Param("campusId") Long campusId);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO res_category (
            campus_id, parent_id, category_type, name, sort_order, status, created_by, updated_by
        ) VALUES (
            #{campusId}, #{parentId}, 'MATERIAL', #{name}, #{sortOrder}, #{status}, #{operatorId}, #{operatorId}
        )
        RETURNING id
        """)
    Long insertCategory(
        @Param("campusId") Long campusId,
        @Param("parentId") Long parentId,
        @Param("name") String name,
        @Param("sortOrder") Integer sortOrder,
        @Param("status") String status,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE res_category
        SET parent_id = #{parentId},
            name = #{name},
            sort_order = #{sortOrder},
            status = #{status},
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND id = #{id}
          AND deleted = 0
        """)
    void updateCategory(
        @Param("campusId") Long campusId,
        @Param("id") Long id,
        @Param("parentId") Long parentId,
        @Param("name") String name,
        @Param("sortOrder") Integer sortOrder,
        @Param("status") String status,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM res_material m
        WHERE m.campus_id = #{campusId}
          AND m.deleted = 0
          AND (CAST(#{categoryId} AS bigint) IS NULL OR m.category_id = #{categoryId})
          AND (CAST(#{resourceType} AS varchar) IS NULL OR m.resource_type = #{resourceType})
          AND (CAST(#{studyType} AS varchar) IS NULL OR m.study_type = #{studyType})
          AND (CAST(#{visibility} AS varchar) IS NULL OR m.visibility = #{visibility})
          AND (CAST(#{status} AS varchar) IS NULL OR m.status = #{status})
          AND (
              CAST(#{keyword} AS varchar) IS NULL
              OR m.title ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%')
              OR m.description ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%')
          )
        """)
    long countMaterials(
        @Param("campusId") Long campusId,
        @Param("categoryId") Long categoryId,
        @Param("keyword") String keyword,
        @Param("resourceType") String resourceType,
        @Param("studyType") String studyType,
        @Param("visibility") String visibility,
        @Param("status") String status
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT m.id,
               m.category_id,
               c.name AS category_name,
               m.title,
               m.description,
               m.resource_type,
               m.cover_file_id,
               cover.url AS cover_url,
               m.file_id,
               rf.file_name,
               m.owner_teacher_id,
               t.name AS owner_teacher_name,
               m.visibility,
               m.study_type,
               m.allow_download,
               m.audit_status,
               m.status,
               (
                   SELECT string_agg(mc.class_id::text, ',')
                   FROM res_material_class mc
                   WHERE mc.campus_id = m.campus_id
                     AND mc.material_id = m.id
                     AND mc.deleted = 0
               ) AS class_ids_csv,
               m.created_at,
               m.updated_at
        FROM res_material m
        JOIN res_category c
          ON c.campus_id = m.campus_id
         AND c.id = m.category_id
         AND c.deleted = 0
        JOIN res_file rf
          ON rf.campus_id = m.campus_id
         AND rf.id = m.file_id
         AND rf.deleted = 0
        LEFT JOIN res_file cover
          ON cover.campus_id = m.campus_id
         AND cover.id = m.cover_file_id
         AND cover.deleted = 0
        LEFT JOIN edu_teacher t
          ON t.campus_id = m.campus_id
         AND t.id = m.owner_teacher_id
         AND t.deleted = 0
        WHERE m.campus_id = #{campusId}
          AND m.deleted = 0
          AND (CAST(#{categoryId} AS bigint) IS NULL OR m.category_id = #{categoryId})
          AND (CAST(#{resourceType} AS varchar) IS NULL OR m.resource_type = #{resourceType})
          AND (CAST(#{studyType} AS varchar) IS NULL OR m.study_type = #{studyType})
          AND (CAST(#{visibility} AS varchar) IS NULL OR m.visibility = #{visibility})
          AND (CAST(#{status} AS varchar) IS NULL OR m.status = #{status})
          AND (
              CAST(#{keyword} AS varchar) IS NULL
              OR m.title ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%')
              OR m.description ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%')
          )
        ORDER BY m.updated_at DESC, m.id DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """)
    List<AdminP1Rows.MaterialRow> selectMaterials(
        @Param("campusId") Long campusId,
        @Param("categoryId") Long categoryId,
        @Param("keyword") String keyword,
        @Param("resourceType") String resourceType,
        @Param("studyType") String studyType,
        @Param("visibility") String visibility,
        @Param("status") String status,
        @Param("pageSize") long pageSize,
        @Param("offset") long offset
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT m.id,
               m.category_id,
               c.name AS category_name,
               m.title,
               m.description,
               m.resource_type,
               m.cover_file_id,
               cover.url AS cover_url,
               m.file_id,
               rf.file_name,
               m.owner_teacher_id,
               t.name AS owner_teacher_name,
               m.visibility,
               m.study_type,
               m.allow_download,
               m.audit_status,
               m.status,
               (
                   SELECT string_agg(mc.class_id::text, ',')
                   FROM res_material_class mc
                   WHERE mc.campus_id = m.campus_id
                     AND mc.material_id = m.id
                     AND mc.deleted = 0
               ) AS class_ids_csv,
               m.created_at,
               m.updated_at
        FROM res_material m
        JOIN res_category c
          ON c.campus_id = m.campus_id
         AND c.id = m.category_id
         AND c.deleted = 0
        JOIN res_file rf
          ON rf.campus_id = m.campus_id
         AND rf.id = m.file_id
         AND rf.deleted = 0
        LEFT JOIN res_file cover
          ON cover.campus_id = m.campus_id
         AND cover.id = m.cover_file_id
         AND cover.deleted = 0
        LEFT JOIN edu_teacher t
          ON t.campus_id = m.campus_id
         AND t.id = m.owner_teacher_id
         AND t.deleted = 0
        WHERE m.campus_id = #{campusId}
          AND m.id = #{id}
          AND m.deleted = 0
        LIMIT 1
        """)
    AdminP1Rows.MaterialRow selectMaterial(@Param("campusId") Long campusId, @Param("id") Long id);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO res_material (
            campus_id, category_id, title, description, resource_type, cover_file_id, file_id,
            owner_teacher_id, visibility, study_type, allow_download, audit_status, audit_by,
            audit_time, status, created_by, updated_by
        ) VALUES (
            #{campusId}, #{categoryId}, #{title}, #{description}, #{resourceType}, #{coverFileId}, #{fileId},
            #{ownerTeacherId}, #{visibility}, #{studyType}, #{allowDownload}, 'APPROVED', #{operatorId},
            NOW(), #{status}, #{operatorId}, #{operatorId}
        )
        RETURNING id
        """)
    Long insertMaterial(
        @Param("campusId") Long campusId,
        @Param("categoryId") Long categoryId,
        @Param("title") String title,
        @Param("description") String description,
        @Param("resourceType") String resourceType,
        @Param("coverFileId") Long coverFileId,
        @Param("fileId") Long fileId,
        @Param("ownerTeacherId") Long ownerTeacherId,
        @Param("visibility") String visibility,
        @Param("studyType") String studyType,
        @Param("allowDownload") Boolean allowDownload,
        @Param("status") String status,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE res_material
        SET category_id = #{categoryId},
            title = #{title},
            description = #{description},
            resource_type = #{resourceType},
            cover_file_id = #{coverFileId},
            file_id = #{fileId},
            owner_teacher_id = #{ownerTeacherId},
            visibility = #{visibility},
            study_type = #{studyType},
            allow_download = #{allowDownload},
            audit_status = 'APPROVED',
            audit_by = #{operatorId},
            audit_time = NOW(),
            status = #{status},
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND id = #{id}
          AND deleted = 0
        """)
    void updateMaterial(
        @Param("campusId") Long campusId,
        @Param("id") Long id,
        @Param("categoryId") Long categoryId,
        @Param("title") String title,
        @Param("description") String description,
        @Param("resourceType") String resourceType,
        @Param("coverFileId") Long coverFileId,
        @Param("fileId") Long fileId,
        @Param("ownerTeacherId") Long ownerTeacherId,
        @Param("visibility") String visibility,
        @Param("studyType") String studyType,
        @Param("allowDownload") Boolean allowDownload,
        @Param("status") String status,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE res_material
        SET status = #{status},
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND id = #{id}
          AND deleted = 0
        """)
    void updateMaterialStatus(
        @Param("campusId") Long campusId,
        @Param("id") Long id,
        @Param("status") String status,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE res_material_class
        SET deleted = 1,
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND material_id = #{materialId}
          AND deleted = 0
        """)
    void clearMaterialClasses(
        @Param("campusId") Long campusId,
        @Param("materialId") Long materialId,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO res_material_class (
            campus_id, material_id, class_id, created_by, updated_by
        ) VALUES (
            #{campusId}, #{materialId}, #{classId}, #{operatorId}, #{operatorId}
        )
        RETURNING id
        """)
    Long insertMaterialClass(
        @Param("campusId") Long campusId,
        @Param("materialId") Long materialId,
        @Param("classId") Long classId,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM ops_activity a
        WHERE a.campus_id = #{campusId}
          AND a.deleted = 0
          AND (CAST(#{status} AS varchar) IS NULL OR a.status = #{status})
          AND (CAST(#{keyword} AS varchar) IS NULL OR a.title ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%'))
        """)
    long countActivities(
        @Param("campusId") Long campusId,
        @Param("keyword") String keyword,
        @Param("status") String status
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT a.id,
               a.title,
               a.description,
               a.cover_file_id,
               cover.url AS cover_url,
               a.start_time,
               a.end_time,
               a.location,
               a.fee,
               a.quota,
               a.registered_count,
               a.status,
               a.published_at,
               a.created_at,
               a.updated_at
        FROM ops_activity a
        LEFT JOIN res_file cover
          ON cover.campus_id = a.campus_id
         AND cover.id = a.cover_file_id
         AND cover.deleted = 0
        WHERE a.campus_id = #{campusId}
          AND a.deleted = 0
          AND (CAST(#{status} AS varchar) IS NULL OR a.status = #{status})
          AND (CAST(#{keyword} AS varchar) IS NULL OR a.title ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%'))
        ORDER BY a.start_time DESC, a.id DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """)
    List<AdminP1Rows.ActivityRow> selectActivities(
        @Param("campusId") Long campusId,
        @Param("keyword") String keyword,
        @Param("status") String status,
        @Param("pageSize") long pageSize,
        @Param("offset") long offset
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT a.id,
               a.title,
               a.description,
               a.cover_file_id,
               cover.url AS cover_url,
               a.start_time,
               a.end_time,
               a.location,
               a.fee,
               a.quota,
               a.registered_count,
               a.status,
               a.published_at,
               a.created_at,
               a.updated_at
        FROM ops_activity a
        LEFT JOIN res_file cover
          ON cover.campus_id = a.campus_id
         AND cover.id = a.cover_file_id
         AND cover.deleted = 0
        WHERE a.campus_id = #{campusId}
          AND a.id = #{id}
          AND a.deleted = 0
        LIMIT 1
        """)
    AdminP1Rows.ActivityRow selectActivity(@Param("campusId") Long campusId, @Param("id") Long id);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO ops_activity (
            campus_id, title, description, cover_file_id, start_time, end_time, location,
            fee, quota, registered_count, status, published_at, created_by, updated_by
        ) VALUES (
            #{campusId}, #{title}, #{description}, #{coverFileId}, #{startTime}, #{endTime}, #{location},
            #{fee}, #{quota}, 0, #{status}, CASE WHEN #{status} = 'PUBLISHED' THEN NOW() ELSE NULL END,
            #{operatorId}, #{operatorId}
        )
        RETURNING id
        """)
    Long insertActivity(
        @Param("campusId") Long campusId,
        @Param("title") String title,
        @Param("description") String description,
        @Param("coverFileId") Long coverFileId,
        @Param("startTime") OffsetDateTime startTime,
        @Param("endTime") OffsetDateTime endTime,
        @Param("location") String location,
        @Param("fee") BigDecimal fee,
        @Param("quota") Integer quota,
        @Param("status") String status,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE ops_activity
        SET title = #{title},
            description = #{description},
            cover_file_id = #{coverFileId},
            start_time = #{startTime},
            end_time = #{endTime},
            location = #{location},
            fee = #{fee},
            quota = #{quota},
            status = #{status},
            published_at = CASE WHEN #{status} = 'PUBLISHED' AND published_at IS NULL THEN NOW() ELSE published_at END,
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND id = #{id}
          AND deleted = 0
        """)
    void updateActivity(
        @Param("campusId") Long campusId,
        @Param("id") Long id,
        @Param("title") String title,
        @Param("description") String description,
        @Param("coverFileId") Long coverFileId,
        @Param("startTime") OffsetDateTime startTime,
        @Param("endTime") OffsetDateTime endTime,
        @Param("location") String location,
        @Param("fee") BigDecimal fee,
        @Param("quota") Integer quota,
        @Param("status") String status,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE ops_activity
        SET status = #{status},
            published_at = CASE WHEN #{status} = 'PUBLISHED' AND published_at IS NULL THEN NOW() ELSE published_at END,
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND id = #{id}
          AND deleted = 0
        """)
    void updateActivityStatus(
        @Param("campusId") Long campusId,
        @Param("id") Long id,
        @Param("status") String status,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM ops_activity_registration reg
        WHERE reg.campus_id = #{campusId}
          AND reg.deleted = 0
          AND (CAST(#{activityId} AS bigint) IS NULL OR reg.activity_id = #{activityId})
          AND (CAST(#{status} AS varchar) IS NULL OR reg.status = #{status})
        """)
    long countRegistrations(
        @Param("campusId") Long campusId,
        @Param("activityId") Long activityId,
        @Param("status") String status
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT reg.id,
               reg.activity_id,
               a.title AS activity_title,
               reg.student_id,
               s.name AS student_name,
               reg.guardian_id,
               reg.order_id,
               reg.registration_no,
               reg.amount,
               reg.status,
               reg.registered_at,
               o.pay_status
        FROM ops_activity_registration reg
        JOIN ops_activity a
          ON a.campus_id = reg.campus_id
         AND a.id = reg.activity_id
         AND a.deleted = 0
        JOIN edu_student s
          ON s.campus_id = reg.campus_id
         AND s.id = reg.student_id
         AND s.deleted = 0
        LEFT JOIN fin_order o
          ON o.campus_id = reg.campus_id
         AND o.id = reg.order_id
         AND o.deleted = 0
        WHERE reg.campus_id = #{campusId}
          AND reg.deleted = 0
          AND (CAST(#{activityId} AS bigint) IS NULL OR reg.activity_id = #{activityId})
          AND (CAST(#{status} AS varchar) IS NULL OR reg.status = #{status})
        ORDER BY reg.registered_at DESC, reg.id DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """)
    List<AdminP1Rows.RegistrationRow> selectRegistrations(
        @Param("campusId") Long campusId,
        @Param("activityId") Long activityId,
        @Param("status") String status,
        @Param("pageSize") long pageSize,
        @Param("offset") long offset
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM fin_order o
        WHERE o.campus_id = #{campusId}
          AND o.deleted = 0
          AND (CAST(#{orderType} AS varchar) IS NULL OR o.order_type = #{orderType})
          AND (CAST(#{payStatus} AS varchar) IS NULL OR o.pay_status = #{payStatus})
          AND (CAST(#{studentId} AS bigint) IS NULL OR o.student_id = #{studentId})
        """)
    long countOrders(
        @Param("campusId") Long campusId,
        @Param("orderType") String orderType,
        @Param("payStatus") String payStatus,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT o.id,
               o.order_no,
               o.order_type,
               o.student_id,
               s.name AS student_name,
               o.guardian_id,
               o.activity_id,
               a.title AS activity_title,
               o.total_amount,
               o.discount_amount,
               o.paid_amount,
               o.pay_status,
               o.pay_channel,
               o.transaction_no,
               o.pay_time,
               o.status,
               o.created_at
        FROM fin_order o
        LEFT JOIN edu_student s
          ON s.campus_id = o.campus_id
         AND s.id = o.student_id
         AND s.deleted = 0
        LEFT JOIN ops_activity a
          ON a.campus_id = o.campus_id
         AND a.id = o.activity_id
         AND a.deleted = 0
        WHERE o.campus_id = #{campusId}
          AND o.deleted = 0
          AND (CAST(#{orderType} AS varchar) IS NULL OR o.order_type = #{orderType})
          AND (CAST(#{payStatus} AS varchar) IS NULL OR o.pay_status = #{payStatus})
          AND (CAST(#{studentId} AS bigint) IS NULL OR o.student_id = #{studentId})
        ORDER BY o.created_at DESC, o.id DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """)
    List<AdminP1Rows.OrderRow> selectOrders(
        @Param("campusId") Long campusId,
        @Param("orderType") String orderType,
        @Param("payStatus") String payStatus,
        @Param("studentId") Long studentId,
        @Param("pageSize") long pageSize,
        @Param("offset") long offset
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM fin_payment_record p
        WHERE p.campus_id = #{campusId}
          AND p.deleted = 0
          AND (CAST(#{status} AS varchar) IS NULL OR p.status = #{status})
          AND (CAST(#{orderId} AS bigint) IS NULL OR p.order_id = #{orderId})
        """)
    long countPayments(
        @Param("campusId") Long campusId,
        @Param("status") String status,
        @Param("orderId") Long orderId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT p.id,
               p.order_id,
               o.order_no,
               p.payment_no,
               p.pay_channel,
               p.amount,
               p.transaction_no,
               p.status,
               p.paid_at,
               p.created_at
        FROM fin_payment_record p
        JOIN fin_order o
          ON o.campus_id = p.campus_id
         AND o.id = p.order_id
         AND o.deleted = 0
        WHERE p.campus_id = #{campusId}
          AND p.deleted = 0
          AND (CAST(#{status} AS varchar) IS NULL OR p.status = #{status})
          AND (CAST(#{orderId} AS bigint) IS NULL OR p.order_id = #{orderId})
        ORDER BY p.created_at DESC, p.id DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """)
    List<AdminP1Rows.PaymentRow> selectPayments(
        @Param("campusId") Long campusId,
        @Param("status") String status,
        @Param("orderId") Long orderId,
        @Param("pageSize") long pageSize,
        @Param("offset") long offset
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM sys_notification n
        WHERE n.campus_id = #{campusId}
          AND n.deleted = 0
          AND (CAST(#{status} AS varchar) IS NULL OR n.status = #{status})
          AND (CAST(#{bizType} AS varchar) IS NULL OR n.biz_type = #{bizType})
          AND (CAST(#{receiverStudentId} AS bigint) IS NULL OR n.receiver_student_id = #{receiverStudentId})
        """)
    long countNotifications(
        @Param("campusId") Long campusId,
        @Param("status") String status,
        @Param("bizType") String bizType,
        @Param("receiverStudentId") Long receiverStudentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT n.id,
               n.receiver_user_id,
               n.receiver_student_id,
               s.name AS receiver_student_name,
               n.biz_type,
               n.biz_id,
               n.title,
               n.content,
               n.status,
               n.read_at,
               n.created_at
        FROM sys_notification n
        LEFT JOIN edu_student s
          ON s.campus_id = n.campus_id
         AND s.id = n.receiver_student_id
         AND s.deleted = 0
        WHERE n.campus_id = #{campusId}
          AND n.deleted = 0
          AND (CAST(#{status} AS varchar) IS NULL OR n.status = #{status})
          AND (CAST(#{bizType} AS varchar) IS NULL OR n.biz_type = #{bizType})
          AND (CAST(#{receiverStudentId} AS bigint) IS NULL OR n.receiver_student_id = #{receiverStudentId})
        ORDER BY n.created_at DESC, n.id DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """)
    List<AdminP1Rows.NotificationRow> selectNotifications(
        @Param("campusId") Long campusId,
        @Param("status") String status,
        @Param("bizType") String bizType,
        @Param("receiverStudentId") Long receiverStudentId,
        @Param("pageSize") long pageSize,
        @Param("offset") long offset
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT id AS student_id
        FROM edu_student
        WHERE campus_id = #{campusId}
          AND deleted = 0
          AND status = 'ACTIVE'
        ORDER BY id
        """)
    List<AdminP1Rows.StudentTargetRow> selectCampusStudents(@Param("campusId") Long campusId);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT cs.student_id
        FROM edu_class_student cs
        JOIN edu_student s
          ON s.campus_id = cs.campus_id
         AND s.id = cs.student_id
         AND s.deleted = 0
         AND s.status = 'ACTIVE'
        WHERE cs.campus_id = #{campusId}
          AND cs.class_id = #{classId}
          AND cs.deleted = 0
          AND cs.status = 'ACTIVE'
        ORDER BY cs.student_id
        """)
    List<AdminP1Rows.StudentTargetRow> selectClassStudents(
        @Param("campusId") Long campusId,
        @Param("classId") Long classId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT id AS student_id
        FROM edu_student
        WHERE campus_id = #{campusId}
          AND id = #{studentId}
          AND deleted = 0
          AND status = 'ACTIVE'
        LIMIT 1
        """)
    AdminP1Rows.StudentTargetRow selectStudentTarget(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO sys_notification (
            campus_id, receiver_user_id, receiver_student_id, biz_type, biz_id, title, content, status,
            created_by, updated_by
        ) VALUES (
            #{campusId}, NULL, #{studentId}, #{bizType}, #{bizId}, #{title}, #{content}, 'UNREAD',
            #{operatorId}, #{operatorId}
        )
        RETURNING id
        """)
    Long insertNotification(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("bizType") String bizType,
        @Param("bizId") Long bizId,
        @Param("title") String title,
        @Param("content") String content,
        @Param("operatorId") Long operatorId
    );
}
