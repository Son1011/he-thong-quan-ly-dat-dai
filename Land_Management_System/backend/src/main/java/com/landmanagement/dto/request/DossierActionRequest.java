package com.landmanagement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierActionRequest {
    
    private String action; // approve, return, escalate, submit
    
    private String note;
    
    private String signatureBase64Png;
}
