package com.corporate.online.learning.platform.dto.response.trainer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathInfoToTrainerResponse {

    private String name;
    private String category;
    private Long currentEnrollments;
    private Integer numberOfCourses;
    private List<CoursesToTrainerResponse> courses;
}
