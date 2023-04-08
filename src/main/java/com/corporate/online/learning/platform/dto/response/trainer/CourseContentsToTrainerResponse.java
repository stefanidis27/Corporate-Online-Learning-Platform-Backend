package com.corporate.online.learning.platform.dto.response.trainer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseContentsToTrainerResponse {

    private String text;
    private Boolean needsGrading;
}
