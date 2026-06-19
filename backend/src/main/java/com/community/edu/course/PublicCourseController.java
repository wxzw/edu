package com.community.edu.course;

import com.community.edu.common.response.ApiResponse;
import com.community.edu.course.dto.PublicCourseResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/miniapp/public")
public class PublicCourseController {

    private final PublicCourseService publicCourseService;

    @GetMapping("/campuses")
    public ApiResponse<List<PublicCourseResponses.CampusItem>> campuses() {
        return ApiResponse.success(publicCourseService.campuses());
    }

    @GetMapping("/courses")
    public ApiResponse<List<PublicCourseResponses.CourseSummary>> courses(
        @RequestParam(required = false) Long campusId,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(publicCourseService.courses(campusId, keyword));
    }

    @GetMapping("/courses/{id}")
    public ApiResponse<PublicCourseResponses.CourseDetail> courseDetail(
        @PathVariable Long id,
        @RequestParam Long campusId
    ) {
        return ApiResponse.success(publicCourseService.courseDetail(campusId, id));
    }
}
