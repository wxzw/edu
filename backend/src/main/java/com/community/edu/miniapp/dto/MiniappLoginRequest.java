package com.community.edu.miniapp.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MiniappLoginRequest {

    @Size(max = 128)
    private String loginCode;

    @Size(max = 128)
    private String phoneCode;

    @Size(max = 32)
    private String roleHint;

    @Size(max = 128)
    private String mockOpenId;

    @Size(max = 128)
    private String mockUnionId;

    @Size(max = 20)
    private String mockPhone;
}
