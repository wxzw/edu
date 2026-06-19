package com.community.edu.course;

import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.common.util.StringUtil;
import com.community.edu.course.dto.CourseAdmissionRows;
import com.community.edu.course.dto.PublicCourseResponses;
import com.community.edu.mapper.CourseAdmissionMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicCourseService {

    private final CourseAdmissionMapper mapper;

    public List<PublicCourseResponses.CampusItem> campuses() {
        return mapper.selectPublicCampuses();
    }

    public List<PublicCourseResponses.CourseSummary> courses(Long campusId, String keyword) {
        return mapper.selectPublicCourses(campusId, StringUtil.blankToNull(keyword)).stream()
            .map(this::toSummary)
            .toList();
    }

    public PublicCourseResponses.CourseDetail courseDetail(Long campusId, Long courseId) {
        CourseAdmissionRows.CourseRow row = mapper.selectPublicCourse(campusId, courseId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Course not found");
        }
        PublicCourseResponses.CourseDetail detail = new PublicCourseResponses.CourseDetail();
        copyCourse(row, detail);
        detail.setPublicDetail(row.getPublicDetail());
        detail.setTeachers(mapper.selectPublicCourseTeachers(campusId, courseId));
        detail.setClasses(mapper.selectPublicCourseClasses(campusId, courseId));
        return detail;
    }

    private PublicCourseResponses.CourseSummary toSummary(CourseAdmissionRows.CourseRow row) {
        PublicCourseResponses.CourseSummary response = new PublicCourseResponses.CourseSummary();
        copyCourse(row, response);
        return response;
    }

    private void copyCourse(CourseAdmissionRows.CourseRow row, PublicCourseResponses.CourseSummary response) {
        response.setId(row.getId());
        response.setCampusId(row.getCampusId());
        response.setCampusName(row.getCampusName());
        response.setCourseSystem(row.getCourseSystem());
        response.setName(row.getName());
        response.setLevelName(row.getLevelName());
        response.setTargetAgeMin(row.getTargetAgeMin());
        response.setTargetAgeMax(row.getTargetAgeMax());
        response.setGradeScope(row.getGradeScope());
        response.setTotalHours(row.getTotalHours());
        response.setUnitPrice(row.getUnitPrice());
        response.setPackagePrice(row.getPackagePrice());
        response.setDescription(row.getDescription());
        response.setPublicSummary(row.getPublicSummary());
        response.setCoverUrl(row.getCoverUrl());
        response.setPublicStatus(row.getPublicStatus());
        response.setTeacherNames(row.getTeacherNames());
        response.setOpenClassCount(row.getOpenClassCount());
    }
}
