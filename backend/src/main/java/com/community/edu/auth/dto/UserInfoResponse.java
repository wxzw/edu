package com.community.edu.auth.dto;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String accountType;
    private Set<String> roles;
    private Set<String> permissions;
    private List<CampusOption> campusList;
    private Long defaultCampusId;
}
