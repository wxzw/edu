package com.community.edu.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.edu.admin.dto.TeacherQuery;
import com.community.edu.admin.dto.TeacherRequest;
import com.community.edu.admin.dto.TeacherResponse;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.common.response.PageResponse;
import com.community.edu.entity.EduTeacher;
import com.community.edu.mapper.EduTeacherMapper;
import com.community.edu.service.CampusScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 老师管理服务。处理老师的CRUD及状态变更。
 */
@Service
@RequiredArgsConstructor
public class AdminTeacherService {

    private final EduTeacherMapper teacherMapper;
    private final CampusScopeService campusScopeService;

    public PageResponse<TeacherResponse> page(TeacherQuery query) {
        campusScopeService.requiredCampusId();
        LambdaQueryWrapper<EduTeacher> wrapper = new LambdaQueryWrapper<EduTeacher>()
            .eq(StringUtils.hasText(query.getStatus()), EduTeacher::getStatus, query.getStatus())
            .and(StringUtils.hasText(query.getKeyword()), item -> item
                .like(EduTeacher::getName, query.getKeyword())
                .or()
                .like(EduTeacher::getEmployeeNo, query.getKeyword())
                .or()
                .like(EduTeacher::getPhone, query.getKeyword()))
            .ge(query.getStartDate() != null, EduTeacher::getHireDate, query.getStartDate())
            .le(query.getEndDate() != null, EduTeacher::getHireDate, query.getEndDate())
            .orderByDesc(EduTeacher::getId);
        Page<EduTeacher> page = teacherMapper.selectPage(Page.of(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords().stream().map(TeacherResponse::from).toList(),
            page.getTotal(), page.getCurrent(), page.getSize());
    }

    public TeacherResponse detail(Long id) {
        campusScopeService.requiredCampusId();
        return TeacherResponse.from(getRequired(id));
    }

    @Transactional
    public TeacherResponse create(TeacherRequest request) {
        EduTeacher teacher = new EduTeacher();
        teacher.setCampusId(campusScopeService.requiredCampusId());
        apply(teacher, request);
        if (!StringUtils.hasText(teacher.getStatus())) {
            teacher.setStatus("ENABLED");
        }
        teacherMapper.insert(teacher);
        return TeacherResponse.from(teacher);
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherRequest request) {
        campusScopeService.requiredCampusId();
        EduTeacher teacher = getRequired(id);
        apply(teacher, request);
        teacherMapper.updateById(teacher);
        return TeacherResponse.from(getRequired(id));
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        campusScopeService.requiredCampusId();
        EduTeacher teacher = getRequired(id);
        teacher.setStatus(status);
        teacherMapper.updateById(teacher);
    }

    private EduTeacher getRequired(Long id) {
        EduTeacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "老师不存在");
        }
        return teacher;
    }

    private void apply(EduTeacher teacher, TeacherRequest request) {
        teacher.setUserId(request.getUserId());
        teacher.setEmployeeNo(request.getEmployeeNo());
        teacher.setName(request.getName());
        teacher.setGender(request.getGender());
        teacher.setPhone(request.getPhone());
        teacher.setTitle(request.getTitle());
        teacher.setIntro(request.getIntro());
        teacher.setHireDate(request.getHireDate());
        teacher.setStatus(request.getStatus());
    }
}
