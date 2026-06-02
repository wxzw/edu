package com.community.edu.miniapp.dto;

import com.community.edu.auth.dto.UserInfoResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MiniappMeResponse {

    private UserInfoResponse userInfo;
    private List<MiniappIdentityResponse> availableIdentities;
    private MiniappIdentityResponse selectedIdentity;
}
