package com.landmanagement.dto.response;

import com.landmanagement.enums.UnitLevel;
import com.landmanagement.enums.UnitKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitResponse {
    
    private Long id;
    
    private String name;
    
    private UnitLevel level;
    
    private UnitKind kind;
    
    private Long parentId;
}
