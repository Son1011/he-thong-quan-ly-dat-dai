package com.landmanagement.enums;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    ADMIN,
    COMMUNE_OFFICER,
    PROVINCE_OFFICER,
    CENTRAL_OFFICER;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
