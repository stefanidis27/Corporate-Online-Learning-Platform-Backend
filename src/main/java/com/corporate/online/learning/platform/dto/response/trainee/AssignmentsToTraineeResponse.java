package com.corporate.online.learning.platform.dto.response.trainee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentsToTraineeResponse {

    private Long id;
    private Boolean needsGrading;
    private Boolean status;
    private String comment;
    private String text;
}
