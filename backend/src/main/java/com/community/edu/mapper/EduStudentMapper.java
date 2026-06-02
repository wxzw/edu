package com.community.edu.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.edu.entity.EduStudent;
import com.community.edu.miniapp.dto.MiniappChildStudentResponse;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface EduStudentMapper extends BaseMapper<EduStudent> {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT *
        FROM edu_student
        WHERE user_id = #{userId}
          AND deleted = 0
        ORDER BY campus_id, id
        """)
    List<EduStudent> selectByUserIdIgnoreTenant(@Param("userId") Long userId);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT s.id AS student_id,
               s.campus_id,
               s.name,
               s.nickname,
               s.avatar_url,
               s.grade,
               s.school,
               sg.relation
        FROM edu_student_guardian sg
        JOIN edu_student s
          ON s.campus_id = sg.campus_id
         AND s.id = sg.student_id
         AND s.deleted = 0
        WHERE sg.guardian_id = #{guardianId}
          AND sg.campus_id = #{campusId}
          AND sg.deleted = 0
          AND s.status = 'ACTIVE'
        ORDER BY sg.is_primary DESC, s.id
        """)
    List<MiniappChildStudentResponse> selectChildrenByGuardian(
        @Param("campusId") Long campusId,
        @Param("guardianId") Long guardianId
    );
}
