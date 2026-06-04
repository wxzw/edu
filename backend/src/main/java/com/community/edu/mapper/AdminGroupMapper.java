package com.community.edu.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.community.edu.admin.dto.AdminGroupRows;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface AdminGroupMapper {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT gr.id,
               gr.request_no,
               COALESCE(s.nickname, s.name, gm.nickname) AS initiator_name,
               gm.phone AS initiator_phone,
               gr.child_age,
               gr.grade,
               gr.target_system,
               gr.english_level,
               gr.preferred_times::text AS preferred_times_json,
               gr.required_members,
               gr.current_members,
               gr.status,
               gr.share_code,
               gr.expires_at,
               gr.created_at
        FROM grp_request gr
        LEFT JOIN edu_student s
          ON s.campus_id = gr.campus_id
         AND s.id = gr.initiator_student_id
         AND s.deleted = 0
        LEFT JOIN LATERAL (
            SELECT *
            FROM grp_member m
            WHERE m.campus_id = gr.campus_id
              AND m.request_id = gr.id
              AND m.deleted = 0
            ORDER BY m.joined_at, m.id
            LIMIT 1
        ) gm ON TRUE
        WHERE gr.campus_id = #{campusId}
          AND gr.deleted = 0
          AND (CAST(#{status} AS varchar) IS NULL OR gr.status = #{status})
        ORDER BY gr.created_at DESC, gr.id DESC
        """)
    List<AdminGroupRows.GroupRequestRow> selectGroupRequests(
        @Param("campusId") Long campusId,
        @Param("status") String status
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT COUNT(*)
        FROM grp_request
        WHERE campus_id = #{campusId}
          AND id = #{requestId}
          AND deleted = 0
        """)
    int countGroupRequest(@Param("campusId") Long campusId, @Param("requestId") Long requestId);

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO grp_trial (
            campus_id, request_id, trial_time, location, teacher_id, class_id, wechat_qr_file_id,
            status, arranged_by, arranged_at, remark, created_by, updated_by
        ) VALUES (
            #{campusId}, #{requestId}, #{trialTime}, #{location}, #{teacherId}, #{classId}, #{wechatQrFileId},
            'ARRANGED', #{arrangedBy}, NOW(), #{remark}, #{arrangedBy}, #{arrangedBy}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertTrial(AdminGroupRows.TrialWrite trial);

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE grp_request
        SET status = #{status},
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND id = #{requestId}
          AND deleted = 0
        """)
    void updateGroupStatus(
        @Param("campusId") Long campusId,
        @Param("requestId") Long requestId,
        @Param("status") String status,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT campus_id
        FROM grp_trial
        WHERE id = #{trialId}
          AND deleted = 0
          AND campus_id = #{campusId}
        LIMIT 1
        """)
    Long selectTrialCampus(@Param("campusId") Long campusId, @Param("trialId") Long trialId);

    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
        INSERT INTO grp_trial_feedback (
            campus_id, trial_id, member_id, student_id, feedback, result, next_action, operator_id, created_by, updated_by
        ) VALUES (
            #{campusId}, #{trialId}, #{memberId}, #{studentId}, #{feedback}, COALESCE(#{result}, 'FOLLOW_UP'),
            #{nextAction}, #{operatorId}, #{operatorId}, #{operatorId}
        )
        """)
    void insertFeedback(
        @Param("campusId") Long campusId,
        @Param("trialId") Long trialId,
        @Param("memberId") Long memberId,
        @Param("studentId") Long studentId,
        @Param("feedback") String feedback,
        @Param("result") String result,
        @Param("nextAction") String nextAction,
        @Param("operatorId") Long operatorId
    );
}
