package com.corporate.online.learning.platform.dto.response.trainer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentsToTrainerResponse {

    private Long id;
    private String name;
    private Integer numberOfAssignments;
    private Integer completedAssignments;
    private String enrollmentDate;
}
