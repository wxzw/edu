package com.community.edu.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.edu.entity.EduTeacher;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface EduTeacherMapper extends BaseMapper<EduTeacher> {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
        SELECT *
        FROM edu_teacher
        WHERE user_id = #{userId}
          AND deleted = 0
        ORDER BY campus_id, id
        """)
    List<EduTeacher> selectByUserIdIgnoreTenant(@Param("userId") Long userId);
}
