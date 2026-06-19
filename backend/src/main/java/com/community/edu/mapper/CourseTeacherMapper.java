package com.community.edu.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface CourseTeacherMapper {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT teacher_id
        FROM edu_course_teacher
        WHERE campus_id = #{campusId}
          AND course_id = #{courseId}
          AND deleted = 0
        ORDER BY sort_order, id
        """)
    List<Long> selectTeacherIds(@Param("campusId") Long campusId, @Param("courseId") Long courseId);

    @InterceptorIgnore(tenantLine = "true")
    @Update("""
        UPDATE edu_course_teacher
        SET deleted = 1,
            updated_at = NOW(),
            updated_by = #{operatorId}
        WHERE campus_id = #{campusId}
          AND course_id = #{courseId}
          AND deleted = 0
        """)
    void clearCourseTeachers(
        @Param("campusId") Long campusId,
        @Param("courseId") Long courseId,
        @Param("operatorId") Long operatorId
    );

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        INSERT INTO edu_course_teacher (
            campus_id, course_id, teacher_id, role_name, sort_order, created_by, updated_by
        ) VALUES (
            #{campusId}, #{courseId}, #{teacherId}, #{roleName}, #{sortOrder}, #{operatorId}, #{operatorId}
        )
        ON CONFLICT (campus_id, course_id, teacher_id) WHERE deleted = 0
        DO UPDATE SET role_name = EXCLUDED.role_name,
                      sort_order = EXCLUDED.sort_order,
                      updated_at = NOW(),
                      updated_by = EXCLUDED.updated_by
        RETURNING id
        """)
    Long insertCourseTeacher(
        @Param("campusId") Long campusId,
        @Param("courseId") Long courseId,
        @Param("teacherId") Long teacherId,
        @Param("roleName") String roleName,
        @Param("sortOrder") Integer sortOrder,
        @Param("operatorId") Long operatorId
    );
}
