package com.corporate.online.learning.platform.dto.response.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursesReportResponse {

    private String name;
    private String category;
    private Boolean selfEnrollment;
    private Integer noAssignments;
    private Long currentEnrollments;
    private Long maxEnrollments;
    private Long completions;
    private Long unEnrollments;
    private Float completionRate;
    private Float dropOutRate;
}
