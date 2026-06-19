package com.community.edu.course;

import com.community.edu.common.response.ApiResponse;
import com.community.edu.course.dto.CourseRegistrationRequests;
import com.community.edu.course.dto.CourseRegistrationResponses;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/miniapp")
public class CourseRegistrationController {

    private final CourseRegistrationService courseRegistrationService;

    @PostMapping("/course-registrations")
    public ApiResponse<CourseRegistrationResponses.RegistrationResult> createRegistration(
        @Valid @RequestBody CourseRegistrationRequests.CreateRegistrationRequest request
    ) {
        return ApiResponse.success(courseRegistrationService.createRegistration(request));
    }

    @GetMapping("/course-registrations/my")
    public ApiResponse<List<CourseRegistrationResponses.RegistrationItem>> myRegistrations() {
        return ApiResponse.success(courseRegistrationService.myRegistrations());
    }

    @PostMapping("/orders/{id}/pay")
    public ApiResponse<CourseRegistrationResponses.MockPayResponse> payCourseOrder(@PathVariable Long id) {
        return ApiResponse.success(courseRegistrationService.payCourseOrder(id));
    }
}
