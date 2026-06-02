package com.community.edu.miniapp.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MiniappIdentityResponse {

    private String identityType;
    private Long identityId;
    private Long campusId;
    private String displayName;
    private String roleName;
    private String avatarUrl;
    private String phone;
    private String status;
    private List<MiniappChildStudentResponse> children;
}
