package com.landmanagement.dto.response;

import com.landmanagement.enums.DossierStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DossierResponse {
    
    private Long id;
    
    private String title;
    
    private DossierStatus status;
    
    private Boolean sentToCentral;
    
    private Long originUnitId;
    
    private Long createdByUserId;
    
    private Long assignedToUnitId;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    private Long dossierTypeId;

    private String dossierTypeName;
}
