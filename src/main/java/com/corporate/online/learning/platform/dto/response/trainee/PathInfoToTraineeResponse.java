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
public class PathInfoToTraineeResponse {

    private String name;
    private String category;
    private Long currentEnrollments;
    private String trainer;
    private Boolean enrollmentStatus;
    private Integer completedCourses;
    private Integer numberOfCourses;
    private List<ExploreCoursesResponse> courses;
}
