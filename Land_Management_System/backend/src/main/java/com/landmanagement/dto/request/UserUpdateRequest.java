package com.landmanagement.dto.request;

import com.landmanagement.enums.UserRole;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    private String fullName;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String phoneNumber;

    private String address;

    private UserRole role;

    private Long unitId;

    private Boolean isActive;

    private Boolean mustChangePassword;
}
