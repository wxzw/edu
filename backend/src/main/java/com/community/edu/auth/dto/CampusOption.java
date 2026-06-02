package com.community.edu.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CampusOption {

    private Long id;
    private String code;
    private String name;
    private String shortName;
    private Boolean isDefault;
}
