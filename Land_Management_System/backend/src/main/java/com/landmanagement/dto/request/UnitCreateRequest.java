package com.landmanagement.dto.request;

import com.landmanagement.enums.UnitLevel;
import com.landmanagement.enums.UnitKind;
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
public class UnitCreateRequest {
    
    @NotBlank(message = "Tên đơn vị không được để trống")
    private String name;
    
    @NotNull(message = "Cấp đơn vị không được để trống")
    private UnitLevel level;
    
    private UnitKind kind;
    
    private Long parentId;
}
