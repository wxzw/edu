package com.community.edu.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.edu.admin.dto.AddClassStudentRequest;
import com.community.edu.admin.dto.ClassQuery;
import com.community.edu.admin.dto.ClassRequest;
import com.community.edu.admin.dto.ClassResponse;
import com.community.edu.admin.dto.ClassStudentResponse;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.common.response.PageResponse;
import com.community.edu.entity.EduClass;
import com.community.edu.entity.EduClassStudent;
import com.community.edu.entity.EduStudent;
import com.community.edu.mapper.EduClassMapper;
import com.community.edu.mapper.EduClassStudentMapper;
import com.community.edu.mapper.EduStudentMapper;
import com.community.edu.service.CampusScopeService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 班级管理服务。处理班级的CRUD、学生添加移除及人数管理。
 */
@Service
@RequiredArgsConstructor
public class AdminClassService {

    private final EduClassMapper classMapper;
    private final EduClassStudentMapper classStudentMapper;
    private final EduStudentMapper studentMapper;
    private final CampusScopeService campusScopeService;

    public PageResponse<ClassResponse> page(ClassQuery query) {
        campusScopeService.requiredCampusId();
        LambdaQueryWrapper<EduClass> wrapper = new LambdaQueryWrapper<EduClass>()
            .eq(query.getCourseId() != null, EduClass::getCourseId, query.getCourseId())
            .eq(StringUtils.hasText(query.getStatus()), EduClass::getStatus, query.getStatus())
            .and(StringUtils.hasText(query.getKeyword()), item -> item
                .like(EduClass::getName, query.getKeyword())
                .or()
                .like(EduClass::getClassNo, query.getKeyword()))
            .orderByDesc(EduClass::getId);
        Page<EduClass> page = classMapper.selectPage(Page.of(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords().stream().map(ClassResponse::from).toList(),
            page.getTotal(), page.getCurrent(), page.getSize());
    }

    public ClassResponse detail(Long id) {
        campusScopeService.requiredCampusId();
        return ClassResponse.from(getRequired(id));
    }

    @Transactional
    public ClassResponse create(ClassRequest request) {
        EduClass eduClass = new EduClass();
        eduClass.setCampusId(campusScopeService.requiredCampusId());
        apply(eduClass, request);
        if (eduClass.getCurrentStudents() == null) {
            eduClass.setCurrentStudents(0);
        }
        if (eduClass.getMaxStudents() == null) {
            eduClass.setMaxStudents(8);
        }
        if (!StringUtils.hasText(eduClass.getStatus())) {
            eduClass.setStatus("PREPARING");
        }
        classMapper.insert(eduClass);
        return ClassResponse.from(eduClass);
    }

    @Transactional
    public ClassResponse update(Long id, ClassRequest request) {
        campusScopeService.requiredCampusId();
        EduClass eduClass = getRequired(id);
        apply(eduClass, request);
        classMapper.updateById(eduClass);
        return ClassResponse.from(getRequired(id));
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        campusScopeService.requiredCampusId();
        EduClass eduClass = getRequired(id);
        eduClass.setStatus(status);
        classMapper.updateById(eduClass);
    }

    private EduClass getRequired(Long id) {
        EduClass eduClass = classMapper.selectById(id);
        if (eduClass == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "班级不存在");
        }
        return eduClass;
    }

    private void apply(EduClass eduClass, ClassRequest request) {
        eduClass.setCourseId(request.getCourseId());
        eduClass.setClassNo(request.getClassNo());
        eduClass.setName(request.getName());
        eduClass.setHeadTeacherId(request.getHeadTeacherId());
        eduClass.setClassroom(request.getClassroom());
        eduClass.setClassWechatQrUrl(request.getClassWechatQrUrl());
        eduClass.setStartDate(request.getStartDate());
        eduClass.setEndDate(request.getEndDate());
        eduClass.setMaxStudents(request.getMaxStudents());
        eduClass.setStatus(request.getStatus());
        eduClass.setRemark(request.getRemark());
    }

    public List<ClassStudentResponse> listStudents(Long classId) {
        campusScopeService.requiredCampusId();
        getRequired(classId);

        LambdaQueryWrapper<EduClassStudent> csWrapper = new LambdaQueryWrapper<EduClassStudent>()
            .eq(EduClassStudent::getClassId, classId)
            .orderByAsc(EduClassStudent::getJoinDate);
        List<EduClassStudent> classStudents = classStudentMapper.selectList(csWrapper);

        if (classStudents.isEmpty()) {
            return List.of();
        }

        List<Long> studentIds = classStudents.stream().map(EduClassStudent::getStudentId).toList();
        LambdaQueryWrapper<EduStudent> sWrapper = new LambdaQueryWrapper<EduStudent>()
            .in(EduStudent::getId, studentIds);
        Map<Long, EduStudent> studentMap = studentMapper.selectList(sWrapper).stream()
            .collect(Collectors.toMap(EduStudent::getId, s -> s));

        return classStudents.stream().map(cs -> {
            EduStudent student = studentMap.get(cs.getStudentId());
            ClassStudentResponse.ClassStudentResponseBuilder builder = ClassStudentResponse.builder()
                .classStudentId(cs.getId())
                .studentId(cs.getStudentId())
                .joinDate(cs.getJoinDate())
                .leaveDate(cs.getLeaveDate())
                .classStudentStatus(cs.getStatus());
            if (student != null) {
                builder.studentNo(student.getStudentNo())
                    .name(student.getName())
                    .nickname(student.getNickname())
                    .gender(student.getGender())
                    .birthday(student.getBirthday())
                    .grade(student.getGrade())
                    .school(student.getSchool())
                    .englishLevel(student.getEnglishLevel())
                    .studentStatus(student.getStatus());
            }
            return builder.build();
        }).toList();
    }

    @Transactional
    public ClassStudentResponse addStudent(Long classId, AddClassStudentRequest request) {
        Long campusId = campusScopeService.requiredCampusId();
        EduClass eduClass = getRequired(classId);

        // 检查学生是否已在班级中
        LambdaQueryWrapper<EduClassStudent> existWrapper = new LambdaQueryWrapper<EduClassStudent>()
            .eq(EduClassStudent::getClassId, classId)
            .eq(EduClassStudent::getStudentId, request.getStudentId());
        if (classStudentMapper.selectCount(existWrapper) > 0) {
            throw new BizException(ErrorCode.BIZ_ERROR, "该学生已在班级中");
        }

        // 检查学生是否存在
        EduStudent student = studentMapper.selectById(request.getStudentId());
        if (student == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "学生不存在");
        }

        // 检查班级人数是否已满
        if (eduClass.getCurrentStudents() != null && eduClass.getMaxStudents() != null
            && eduClass.getCurrentStudents() >= eduClass.getMaxStudents()) {
            throw new BizException(ErrorCode.BIZ_ERROR, "班级人数已满");
        }

        // 创建班级学生关系
        EduClassStudent classStudent = new EduClassStudent();
        classStudent.setCampusId(campusId);
        classStudent.setClassId(classId);
        classStudent.setStudentId(request.getStudentId());
        classStudent.setJoinDate(request.getJoinDate() != null ? request.getJoinDate() : LocalDate.now());
        classStudent.setStatus("ACTIVE");
        classStudentMapper.insert(classStudent);

        // 更新班级人数
        eduClass.setCurrentStudents((eduClass.getCurrentStudents() != null ? eduClass.getCurrentStudents() : 0) + 1);
        classMapper.updateById(eduClass);

        return ClassStudentResponse.builder()
            .classStudentId(classStudent.getId())
            .studentId(student.getId())
            .studentNo(student.getStudentNo())
            .name(student.getName())
            .nickname(student.getNickname())
            .gender(student.getGender())
            .birthday(student.getBirthday())
            .grade(student.getGrade())
            .school(student.getSchool())
            .englishLevel(student.getEnglishLevel())
            .studentStatus(student.getStatus())
            .joinDate(classStudent.getJoinDate())
            .classStudentStatus(classStudent.getStatus())
            .build();
    }

    @Transactional
    public void removeStudent(Long classId, Long studentId) {
        campusScopeService.requiredCampusId();
        EduClass eduClass = getRequired(classId);

        // 查找班级学生关系
        LambdaQueryWrapper<EduClassStudent> wrapper = new LambdaQueryWrapper<EduClassStudent>()
            .eq(EduClassStudent::getClassId, classId)
            .eq(EduClassStudent::getStudentId, studentId);
        EduClassStudent classStudent = classStudentMapper.selectOne(wrapper);

        if (classStudent == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "该学生不在班级中");
        }

        // 逻辑删除
        classStudentMapper.deleteById(classStudent.getId());

        // 更新班级人数
        if (eduClass.getCurrentStudents() != null && eduClass.getCurrentStudents() > 0) {
            eduClass.setCurrentStudents(eduClass.getCurrentStudents() - 1);
            classMapper.updateById(eduClass);
        }
    }
}
