package com.corporate.online.learning.platform.dto.response.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursesToHRResponse {

    private Long id;
    private String name;
    private String category;
    private Integer noAssignments;
    private Long currentEnrollments;
    private Long maxEnrollments;
    private Boolean canEnroll;
}
