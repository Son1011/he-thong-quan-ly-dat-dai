package com.landmanagement.dto.response;

import com.landmanagement.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeResponse {
    private Long id;
    private String username;
    private UserRole role;
    private Long unitId;
    private Boolean mustChangePassword;
}
