package com.corporate.online.learning.platform.dto.response.trainee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentsToTraineeResponse {

    private String name;
    private Integer numberOfAssignments;
    private Integer completedAssignments;
    private String enrollmentDate;
}
