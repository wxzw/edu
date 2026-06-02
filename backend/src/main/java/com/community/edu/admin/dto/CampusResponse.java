package com.community.edu.admin.dto;

import com.community.edu.entity.SysCampus;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CampusResponse {

    private Long id;
    private String code;
    private String name;
    private String shortName;
    private String contactName;
    private String contactPhone;
    private String address;
    private String businessHours;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static CampusResponse from(SysCampus campus) {
        return CampusResponse.builder()
            .id(campus.getId())
            .code(campus.getCode())
            .name(campus.getName())
            .shortName(campus.getShortName())
            .contactName(campus.getContactName())
            .contactPhone(campus.getContactPhone())
            .address(campus.getAddress())
            .businessHours(campus.getBusinessHours())
            .status(campus.getStatus())
            .createdAt(campus.getCreatedAt())
            .updatedAt(campus.getUpdatedAt())
            .build();
    }
}
