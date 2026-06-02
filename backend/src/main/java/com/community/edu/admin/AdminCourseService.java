package com.community.edu.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.edu.admin.dto.CourseQuery;
import com.community.edu.admin.dto.CourseRequest;
import com.community.edu.admin.dto.CourseResponse;
import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.common.response.PageResponse;
import com.community.edu.entity.EduCourse;
import com.community.edu.mapper.EduCourseMapper;
import com.community.edu.service.CampusScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminCourseService {

    private final EduCourseMapper courseMapper;
    private final CampusScopeService campusScopeService;

    public PageResponse<CourseResponse> page(CourseQuery query) {
        campusScopeService.requiredCampusId();
        LambdaQueryWrapper<EduCourse> wrapper = new LambdaQueryWrapper<EduCourse>()
            .eq(StringUtils.hasText(query.getStatus()), EduCourse::getStatus, query.getStatus())
            .eq(StringUtils.hasText(query.getCourseSystem()), EduCourse::getCourseSystem, query.getCourseSystem())
            .and(StringUtils.hasText(query.getKeyword()), item -> item
                .like(EduCourse::getName, query.getKeyword())
                .or()
                .like(EduCourse::getCourseCode, query.getKeyword()))
            .orderByDesc(EduCourse::getId);
        Page<EduCourse> page = courseMapper.selectPage(Page.of(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords().stream().map(CourseResponse::from).toList(),
            page.getTotal(), page.getCurrent(), page.getSize());
    }

    public CourseResponse detail(Long id) {
        campusScopeService.requiredCampusId();
        return CourseResponse.from(getRequired(id));
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        EduCourse course = new EduCourse();
        course.setCampusId(campusScopeService.requiredCampusId());
        apply(course, request);
        if (!StringUtils.hasText(course.getStatus())) {
            course.setStatus("ENABLED");
        }
        courseMapper.insert(course);
        return CourseResponse.from(course);
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        campusScopeService.requiredCampusId();
        EduCourse course = getRequired(id);
        apply(course, request);
        courseMapper.updateById(course);
        return CourseResponse.from(getRequired(id));
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        campusScopeService.requiredCampusId();
        EduCourse course = getRequired(id);
        course.setStatus(status);
        courseMapper.updateById(course);
    }

    private EduCourse getRequired(Long id) {
        EduCourse course = courseMapper.selectById(id);
        if (course == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "课程不存在");
        }
        return course;
    }

    private void apply(EduCourse course, CourseRequest request) {
        course.setCourseCode(request.getCourseCode());
        course.setCourseSystem(request.getCourseSystem());
        course.setName(request.getName());
        course.setLevelName(request.getLevelName());
        course.setTargetAgeMin(request.getTargetAgeMin());
        course.setTargetAgeMax(request.getTargetAgeMax());
        course.setGradeScope(request.getGradeScope());
        course.setTotalHours(request.getTotalHours());
        course.setUnitPrice(request.getUnitPrice());
        course.setPackagePrice(request.getPackagePrice());
        course.setDescription(request.getDescription());
        course.setStatus(request.getStatus());
    }
}
