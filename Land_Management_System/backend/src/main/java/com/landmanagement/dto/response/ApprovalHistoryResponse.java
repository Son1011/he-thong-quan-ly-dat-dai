package com.landmanagement.dto.response;

import com.landmanagement.enums.ApprovalAction;
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
public class ApprovalHistoryResponse {
    
    private Long id;
    
    private Long dossierId;
    
    private ApprovalAction action;
    
    private String note;
    
    private String signatureBase64Png;
    
    private Long actorUserId;
    
    private String actorUsername;
    
    private Long actorUnitId;
    
    private String actorUnitName;
    
    private Long fromUnitId;
    
    private Long toUnitId;
    
    private LocalDateTime createdAt;
}
