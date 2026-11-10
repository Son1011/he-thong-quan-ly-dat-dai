package com.landmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierCreateRequest {

    @NotBlank(message = "Tiêu đề hồ sơ không được để trống")
    private String title;

    @NotBlank(message = "Tên công dân không được để trống")
    private String citizenName;

    @NotBlank(message = "Số CCCD/CMND không được để trống")
    private String citizenIdentityNumber;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String citizenPhone;

    @NotBlank(message = "Địa chỉ công dân không được để trống")
    private String citizenAddress;

    @NotNull(message = "Loại hồ sơ không được để trống")
    private Long dossierTypeId;

    private String priority;
}