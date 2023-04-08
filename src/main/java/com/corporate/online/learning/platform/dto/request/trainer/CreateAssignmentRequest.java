package com.corporate.online.learning.platform.dto.request.trainer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentRequest {

    private String text;
    private Boolean needsGrading;
}
