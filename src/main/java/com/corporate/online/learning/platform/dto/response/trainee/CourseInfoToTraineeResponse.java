package com.corporate.online.learning.platform.dto.response.trainee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseInfoToTraineeResponse {

    private String name;
    private String category;
    private Long currentEnrollments;
    private Long maxEnrollments;
    private List<String> trainers;
    private Boolean enrollmentStatus;
    private Integer completedAssignments;
    private Integer numberOfAssignments;
    private String enrollmentAction;
    private String enrollmentDate;
    private String description;
}
