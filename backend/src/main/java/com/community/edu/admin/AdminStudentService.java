package com.community.edu.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.edu.admin.dto.StudentQuery;
import com.community.edu.admin.dto.StudentRequest;
import com.community.edu.admin.dto.StudentResponse;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.common.response.PageResponse;
import com.community.edu.entity.EduStudent;
import com.community.edu.mapper.EduStudentMapper;
import com.community.edu.service.CampusScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 学生管理服务。处理学生的CRUD及状态变更。
 */
@Service
@RequiredArgsConstructor
public class AdminStudentService {

    private final EduStudentMapper studentMapper;
    private final CampusScopeService campusScopeService;

    public PageResponse<StudentResponse> page(StudentQuery query) {
        campusScopeService.requiredCampusId();
        LambdaQueryWrapper<EduStudent> wrapper = new LambdaQueryWrapper<EduStudent>()
            .eq(StringUtils.hasText(query.getStatus()), EduStudent::getStatus, query.getStatus())
            .eq(StringUtils.hasText(query.getGrade()), EduStudent::getGrade, query.getGrade())
            .and(StringUtils.hasText(query.getKeyword()), item -> item
                .like(EduStudent::getName, query.getKeyword())
                .or()
                .like(EduStudent::getStudentNo, query.getKeyword())
                .or()
                .like(EduStudent::getNickname, query.getKeyword())
                .or()
                .like(EduStudent::getSchool, query.getKeyword()))
            .orderByDesc(EduStudent::getId);
        Page<EduStudent> page = studentMapper.selectPage(Page.of(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords().stream().map(StudentResponse::from).toList(),
            page.getTotal(), page.getCurrent(), page.getSize());
    }

    public StudentResponse detail(Long id) {
        campusScopeService.requiredCampusId();
        return StudentResponse.from(getRequired(id));
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        EduStudent student = new EduStudent();
        student.setCampusId(campusScopeService.requiredCampusId());
        apply(student, request);
        if (!StringUtils.hasText(student.getStatus())) {
            student.setStatus("ACTIVE");
        }
        studentMapper.insert(student);
        return StudentResponse.from(student);
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        campusScopeService.requiredCampusId();
        EduStudent student = getRequired(id);
        apply(student, request);
        studentMapper.updateById(student);
        return StudentResponse.from(getRequired(id));
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        campusScopeService.requiredCampusId();
        EduStudent student = getRequired(id);
        student.setStatus(status);
        studentMapper.updateById(student);
    }

    private EduStudent getRequired(Long id) {
        EduStudent student = studentMapper.selectById(id);
        if (student == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "学生不存在");
        }
        return student;
    }

    private void apply(EduStudent student, StudentRequest request) {
        student.setUserId(request.getUserId());
        student.setStudentNo(request.getStudentNo());
        student.setName(request.getName());
        student.setNickname(request.getNickname());
        student.setAvatarUrl(request.getAvatarUrl());
        student.setGender(request.getGender());
        student.setBirthday(request.getBirthday());
        student.setGrade(request.getGrade());
        student.setSchool(request.getSchool());
        student.setEnglishLevel(request.getEnglishLevel());
        student.setLearningGoal(request.getLearningGoal());
        student.setStatus(request.getStatus());
        student.setEnrolledAt(request.getEnrolledAt());
    }
}