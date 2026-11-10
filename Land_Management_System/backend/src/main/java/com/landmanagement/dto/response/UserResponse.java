package com.landmanagement.dto.response;

import com.landmanagement.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    
    private Long id;
    
    private String username;
    
    private UserRole role;
    
    private Long unitId;
    
    private Boolean isActive;
    
    private Boolean mustChangePassword;
    
    private LocalDateTime last_login_at;
}
