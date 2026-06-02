package com.community.edu.common.context;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class CurrentUser implements Serializable {

    private final Long userId;
    private final String username;
    private final String realName;
    private final String accountType;
    private final Set<String> roleCodes;
    private final Set<String> permissions;
    private final List<Long> campusIds;
    private final Long defaultCampusId;
    private final Long selectedCampusId;

    public boolean isSuperAdmin() {
        return roleCodes != null && roleCodes.contains("SUPER_ADMIN");
    }

    public boolean hasPermission(String permission) {
        return isSuperAdmin() || (permissions != null && permissions.contains(permission));
    }

    public Set<String> roleCodes() {
        return roleCodes == null ? Collections.emptySet() : roleCodes;
    }

    public Set<String> permissions() {
        return permissions == null ? Collections.emptySet() : permissions;
    }

    public List<Long> campusIds() {
        return campusIds == null ? Collections.emptyList() : campusIds;
    }
}
