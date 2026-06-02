package com.community.edu.miniapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MiniappSelectIdentityRequest {

    @NotBlank
    @Size(max = 32)
    private String identityType;

    @NotNull
    private Long identityId;
}
