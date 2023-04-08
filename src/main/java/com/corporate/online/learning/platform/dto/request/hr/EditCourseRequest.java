package com.corporate.online.learning.platform.dto.request.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditCourseRequest {

    private String name;
    private String category;
    private Long maxEnrollments;
    private Boolean selfEnrollment;
    private String description;
}
