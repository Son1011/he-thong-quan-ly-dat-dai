package com.landmanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsResponse {
    @JsonProperty("unit_id")
    private Long unitId;

    private String name;

    private String kind;

    @JsonProperty("total_dossiers")
    private Integer totalDossiers;

    private Integer pending;

    private Integer approved;

    private Integer returned;

    @JsonProperty("total_attachments")
    private Integer totalAttachments;
}
