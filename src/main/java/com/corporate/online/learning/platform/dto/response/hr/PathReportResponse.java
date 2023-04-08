package com.corporate.online.learning.platform.dto.response.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathReportResponse {

    private String name;
    private String category;
    private Integer noCourses;
    private Long currentEnrollments;
    private Long completions;
    private String trainer;
}
