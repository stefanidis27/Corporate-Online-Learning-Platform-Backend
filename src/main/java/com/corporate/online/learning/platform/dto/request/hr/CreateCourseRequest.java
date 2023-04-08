package com.corporate.online.learning.platform.dto.request.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequest {

    private String name;
    private String category;
    private Long maxEnrollments;
    private Boolean selfEnrollment;
    private List<Long> trainerIds;
}
