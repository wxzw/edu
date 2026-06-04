package com.community.edu.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.community.edu.student.dto.StudentP1Rows;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface StudentP1Mapper {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT DISTINCT c.id,
               c.parent_id,
               c.name,
               c.sort_order
        FROM res_category c
        JOIN res_material m
          ON m.campus_id = c.campus_id
         AND m.category_id = c.id
         AND m.deleted = 0
         AND m.status = 'PUBLISHED'
         AND m.audit_status = 'APPROVED'
        WHERE c.campus_id = #{campusId}
          AND c.deleted = 0
          AND c.category_type = 'MATERIAL'
          AND c.status = 'ENABLED'
          AND (
              m.visibility = 'CAMPUS'
              OR EXISTS (
                  SELECT 1
                  FROM res_material_class mc
                  JOIN edu_class_student cs
                    ON cs.campus_id = mc.campus_id
                   AND cs.class_id = mc.class_id
                   AND cs.student_id = #{studentId}
                   AND cs.deleted = 0
                   AND cs.status = 'ACTIVE'
                  WHERE mc.campus_id = m.campus_id
                    AND mc.material_id = m.id
                    AND mc.deleted = 0
              )
          )
        ORDER BY c.sort_order, c.id
        """)
    List<StudentP1Rows.MaterialCategoryRow> selectVisibleMaterialCategories(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT m.id,
               m.category_id,
               c.name AS category_name,
               m.title,
               m.description,
               m.resource_type,
               m.study_type,
               m.allow_download,
               rf.id AS file_id,
               rf.file_name,
               rf.url AS file_url,
               rf.content_type,
               rf.file_size,
               cover.url AS cover_url,
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
        WHERE m.campus_id = #{campusId}
          AND m.deleted = 0
          AND m.status = 'PUBLISHED'
          AND m.audit_status = 'APPROVED'
          AND (CAST(#{categoryId} AS bigint) IS NULL OR m.category_id = #{categoryId})
          AND (CAST(#{studyType} AS varchar) IS NULL OR m.study_type = #{studyType})
          AND (CAST(#{resourceType} AS varchar) IS NULL OR m.resource_type = #{resourceType})
          AND (
              CAST(#{keyword} AS varchar) IS NULL
              OR m.title ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%')
              OR m.description ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%')
          )
          AND (
              m.visibility = 'CAMPUS'
              OR EXISTS (
                  SELECT 1
                  FROM res_material_class mc
                  JOIN edu_class_student cs
                    ON cs.campus_id = mc.campus_id
                   AND cs.class_id = mc.class_id
                   AND cs.student_id = #{studentId}
                   AND cs.deleted = 0
                   AND cs.status = 'ACTIVE'
                  WHERE mc.campus_id = m.campus_id
                    AND mc.material_id = m.id
                    AND mc.deleted = 0
              )
          )
        ORDER BY
          CASE WHEN m.study_type = 'REQUIRED' THEN 0 ELSE 1 END,
          m.updated_at DESC,
          m.id DESC
        """)
    List<StudentP1Rows.MaterialRow> selectVisibleMaterials(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("categoryId") Long categoryId,
        @Param("studyType") String studyType,
        @Param("resourceType") String resourceType,
        @Param("keyword") String keyword
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT *
        FROM (
            SELECT m.id,
                   m.category_id,
                   c.name AS category_name,
                   m.title,
                   m.description,
                   m.resource_type,
                   m.study_type,
                   m.allow_download,
                   rf.id AS file_id,
                   rf.file_name,
                   rf.url AS file_url,
                   rf.content_type,
                   rf.file_size,
                   cover.url AS cover_url,
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
            WHERE m.campus_id = #{campusId}
              AND m.id = #{materialId}
              AND m.deleted = 0
              AND m.status = 'PUBLISHED'
              AND m.audit_status = 'APPROVED'
              AND (
                  m.visibility = 'CAMPUS'
                  OR EXISTS (
                      SELECT 1
                      FROM res_material_class mc
                      JOIN edu_class_student cs
                        ON cs.campus_id = mc.campus_id
                       AND cs.class_id = mc.class_id
                       AND cs.student_id = #{studentId}
                       AND cs.deleted = 0
                       AND cs.status = 'ACTIVE'
                      WHERE mc.campus_id = m.campus_id
                        AND mc.material_id = m.id
                        AND mc.deleted = 0
                  )
              )
        ) item
        LIMIT 1
        """)
    StudentP1Rows.MaterialRow selectVisibleMaterialById(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("materialId") Long materialId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT rf.id AS file_id,
               rf.storage_type,
               rf.object_key,
               rf.url,
               rf.file_name,
               rf.content_type,
               rf.file_size,
               m.allow_download
        FROM res_material m
        JOIN res_file rf
          ON rf.campus_id = m.campus_id
         AND rf.id = m.file_id
         AND rf.deleted = 0
        WHERE m.campus_id = #{campusId}
          AND m.id = #{materialId}
          AND m.deleted = 0
          AND m.status = 'PUBLISHED'
          AND m.audit_status = 'APPROVED'
          AND (
              m.visibility = 'CAMPUS'
              OR EXISTS (
                  SELECT 1
                  FROM res_material_class mc
                  JOIN edu_class_student cs
                    ON cs.campus_id = mc.campus_id
                   AND cs.class_id = mc.class_id
                   AND cs.student_id = #{studentId}
                   AND cs.deleted = 0
                   AND cs.status = 'ACTIVE'
                  WHERE mc.campus_id = m.campus_id
                    AND mc.material_id = m.id
                    AND mc.deleted = 0
              )
          )
        LIMIT 1
        """)
    StudentP1Rows.FileRow selectVisibleMaterialFile(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("materialId") Long materialId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT a.id,
               a.title,
               a.description,
               cover.url AS cover_url,
               a.start_time,
               a.end_time,
               a.location,
               a.fee,
               a.quota,
               a.registered_count,
               a.status,
               a.published_at,
               reg.id AS registration_id,
               reg.status AS registration_status,
               o.id AS order_id,
               o.pay_status
        FROM ops_activity a
        LEFT JOIN res_file cover
          ON cover.campus_id = a.campus_id
         AND cover.id = a.cover_file_id
         AND cover.deleted = 0
        LEFT JOIN ops_activity_registration reg
          ON reg.campus_id = a.campus_id
         AND reg.activity_id = a.id
         AND reg.student_id = #{studentId}
         AND reg.deleted = 0
        LEFT JOIN fin_order o
          ON o.campus_id = a.campus_id
         AND o.activity_id = a.id
         AND o.student_id = #{studentId}
         AND o.order_type = 'ACTIVITY'
         AND o.deleted = 0
        WHERE a.campus_id = #{campusId}
          AND a.deleted = 0
          AND a.status = 'PUBLISHED'
          AND (CAST(#{keyword} AS varchar) IS NULL OR a.title ILIKE CONCAT('%', CAST(#{keyword} AS varchar), '%'))
        ORDER BY a.start_time, a.id
        """)
    List<StudentP1Rows.ActivityRow> selectActivities(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("keyword") String keyword
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT a.id,
               a.title,
               a.description,
               cover.url AS cover_url,
               a.start_time,
               a.end_time,
               a.location,
               a.fee,
               a.quota,
               a.registered_count,
               a.status,
               a.published_at,
               reg.id AS registration_id,
               reg.status AS registration_status,
               o.id AS order_id,
               o.pay_status
        FROM ops_activity a
        LEFT JOIN res_file cover
          ON cover.campus_id = a.campus_id
         AND cover.id = a.cover_file_id
         AND cover.deleted = 0
        LEFT JOIN ops_activity_registration reg
          ON reg.campus_id = a.campus_id
         AND reg.activity_id = a.id
         AND reg.student_id = #{studentId}
         AND reg.deleted = 0
        LEFT JOIN fin_order o
          ON o.campus_id = a.campus_id
         AND o.activity_id = a.id
         AND o.student_id = #{studentId}
         AND o.order_type = 'ACTIVITY'
         AND o.deleted = 0
        WHERE a.campus_id = #{campusId}
          AND a.id = #{activityId}
          AND a.deleted = 0
          AND a.status = 'PUBLISHED'
        LIMIT 1
        """)
    StudentP1Rows.ActivityRow selectActivityDetail(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("activityId") Long activityId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT a.id,
               a.title,
               a.description,
               cover.url AS cover_url,
               a.start_time,
               a.end_time,
               a.location,
               a.fee,
               a.quota,
               a.registered_count,
               a.status,
               a.published_at,
               NULL AS registration_id,
               NULL AS registration_status,
               NULL AS order_id,
               NULL AS pay_status
        FROM ops_activity a
        LEFT JOIN res_file cover
          ON cover.campus_id = a.campus_id
         AND cover.id = a.cover_file_id
         AND cover.deleted = 0
        WHERE a.campus_id = #{campusId}
          AND a.id = #{activityId}
          AND a.deleted = 0
        LIMIT 1
        FOR UPDATE OF a
        """)
    StudentP1Rows.ActivityRow selectActivityForUpdate(
        @Param("campusId") Long campusId,
        @Param("activityId") Long activityId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT id
        FROM ops_activity_registration
        WHERE campus_id = #{campusId}
          AND activity_id = #{activityId}
          AND student_id = #{studentId}
          AND deleted = 0
        LIMIT 1
        """)
    Long selectRegistrationId(
        @Param("campusId") Long campusId,
        @Param("activityId") Long activityId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT id
        FROM fin_order
        WHERE campus_id = #{campusId}
          AND activity_id = #{activityId}
          AND student_id = #{studentId}
          AND order_type = 'ACTIVITY'
          AND pay_status = 'UNPAID'
          AND deleted = 0
        ORDER BY id DESC
        LIMIT 1
        """)
    Long selectUnpaidActivityOrderId(
        @Param("campusId") Long campusId,
        @Param("activityId") Long activityId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO fin_order (
            campus_id, order_no, order_type, student_id, guardian_id, activity_id,
            total_amount, discount_amount, paid_amount, pay_status, status, remark, created_by, updated_by
        ) VALUES (
            #{campusId}, #{orderNo}, 'ACTIVITY', #{studentId}, #{guardianId}, #{activityId},
            #{amount}, 0, 0, 'UNPAID', 'CREATED', #{remark}, #{operatorId}, #{operatorId}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertActivityOrder(StudentP1Rows.OrderWrite order);

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO ops_activity_registration (
            campus_id, activity_id, student_id, guardian_id, order_id, registration_no, amount, status,
            created_by, updated_by
        ) VALUES (
            #{campusId}, #{activityId}, #{studentId}, #{guardianId}, #{orderId}, #{registrationNo}, #{amount}, 'REGISTERED',
            #{operatorId}, #{operatorId}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertRegistration(StudentP1Rows.RegistrationWrite registration);

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE ops_activity
        SET registered_count = registered_count + 1,
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND id = #{activityId}
          AND deleted = 0
        """)
    void incrementActivityRegistrationCount(
        @Param("campusId") Long campusId,
        @Param("activityId") Long activityId,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT o.id,
               o.order_no,
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
               o.remark,
               o.created_at
        FROM fin_order o
        LEFT JOIN ops_activity a
          ON a.campus_id = o.campus_id
         AND a.id = o.activity_id
         AND a.deleted = 0
        WHERE o.campus_id = #{campusId}
          AND o.id = #{orderId}
          AND o.student_id = #{studentId}
          AND o.order_type = 'ACTIVITY'
          AND o.deleted = 0
        LIMIT 1
        """)
    StudentP1Rows.OrderRow selectOrderDetail(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("orderId") Long orderId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT o.id,
               o.order_no,
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
               o.remark,
               o.created_at
        FROM fin_order o
        JOIN ops_activity a
          ON a.campus_id = o.campus_id
         AND a.id = o.activity_id
         AND a.deleted = 0
        WHERE o.campus_id = #{campusId}
          AND o.id = #{orderId}
          AND o.student_id = #{studentId}
          AND o.order_type = 'ACTIVITY'
          AND o.deleted = 0
        LIMIT 1
        FOR UPDATE OF o
        """)
    StudentP1Rows.OrderRow selectOrderForUpdate(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("orderId") Long orderId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE fin_order
        SET paid_amount = total_amount - discount_amount,
            pay_status = 'PAID',
            pay_channel = 'MOCK',
            transaction_no = #{transactionNo},
            pay_time = NOW(),
            status = 'COMPLETED',
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND id = #{orderId}
          AND student_id = #{studentId}
          AND pay_status = 'UNPAID'
          AND deleted = 0
        """)
    void markOrderPaid(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("orderId") Long orderId,
        @Param("transactionNo") String transactionNo,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO fin_payment_record (
            campus_id, order_id, payment_no, pay_channel, amount, transaction_no, status, paid_at,
            raw_response, created_by, updated_by
        ) VALUES (
            #{campusId}, #{orderId}, #{paymentNo}, 'MOCK', #{amount}, #{transactionNo}, 'SUCCESS', NOW(),
            '{"mock":true}'::JSONB, #{operatorId}, #{operatorId}
        )
        """)
    void insertPaymentRecord(
        @Param("campusId") Long campusId,
        @Param("orderId") Long orderId,
        @Param("paymentNo") String paymentNo,
        @Param("amount") BigDecimal amount,
        @Param("transactionNo") String transactionNo,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT reg.id,
               reg.activity_id,
               reg.order_id,
               reg.registration_no,
               reg.amount,
               reg.status,
               reg.registered_at,
               a.title AS activity_title,
               a.start_time,
               a.end_time,
               a.location,
               o.pay_status
        FROM ops_activity_registration reg
        JOIN ops_activity a
          ON a.campus_id = reg.campus_id
         AND a.id = reg.activity_id
         AND a.deleted = 0
        LEFT JOIN fin_order o
          ON o.campus_id = reg.campus_id
         AND o.id = reg.order_id
         AND o.deleted = 0
        WHERE reg.campus_id = #{campusId}
          AND reg.student_id = #{studentId}
          AND reg.deleted = 0
        ORDER BY reg.registered_at DESC, reg.id DESC
        """)
    List<StudentP1Rows.RegistrationRow> selectRegistrations(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT n.id,
               n.biz_type,
               n.biz_id,
               n.title,
               n.content,
               n.status,
               n.read_at,
               n.created_at
        FROM sys_notification n
        WHERE n.campus_id = #{campusId}
          AND n.deleted = 0
          AND (n.receiver_student_id = #{studentId} OR n.receiver_user_id = #{userId})
          AND (CAST(#{status} AS varchar) IS NULL OR n.status = #{status})
        ORDER BY n.created_at DESC, n.id DESC
        LIMIT #{limit}
        """)
    List<StudentP1Rows.NotificationRow> selectNotifications(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("userId") Long userId,
        @Param("status") String status,
        @Param("limit") int limit
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM sys_notification n
        WHERE n.campus_id = #{campusId}
          AND n.deleted = 0
          AND n.status = 'UNREAD'
          AND (n.receiver_student_id = #{studentId} OR n.receiver_user_id = #{userId})
        """)
    Long countUnreadNotifications(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("userId") Long userId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE sys_notification
        SET status = 'READ',
            read_at = COALESCE(read_at, NOW()),
            updated_at = NOW(),
            updated_by = #{userId}
        WHERE campus_id = #{campusId}
          AND id = #{notificationId}
          AND deleted = 0
          AND (receiver_student_id = #{studentId} OR receiver_user_id = #{userId})
        """)
    void markNotificationRead(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("userId") Long userId,
        @Param("notificationId") Long notificationId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE sys_notification
        SET status = 'READ',
            read_at = COALESCE(read_at, NOW()),
            updated_at = NOW(),
            updated_by = #{userId}
        WHERE campus_id = #{campusId}
          AND deleted = 0
          AND status = 'UNREAD'
          AND (receiver_student_id = #{studentId} OR receiver_user_id = #{userId})
        """)
    void markAllNotificationsRead(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("userId") Long userId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO sys_notification (
            campus_id, receiver_user_id, receiver_student_id, biz_type, biz_id, title, content, status,
            created_by, updated_by
        ) VALUES (
            #{campusId}, NULL, #{studentId}, #{bizType}, #{bizId}, #{title}, #{content}, 'UNREAD',
            #{operatorId}, #{operatorId}
        )
        """)
    void insertStudentNotification(
        @Param("campusId") Long campusId,
        @Param("studentId") Long studentId,
        @Param("bizType") String bizType,
        @Param("bizId") Long bizId,
        @Param("title") String title,
        @Param("content") String content,
        @Param("operatorId") Long operatorId
    );
}
