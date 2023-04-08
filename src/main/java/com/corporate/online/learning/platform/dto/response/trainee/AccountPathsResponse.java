package com.corporate.online.learning.platform.dto.response.trainee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountPathsResponse {

    private Long id;
    private String name;
    private String category;
    private Integer numberOfCourses;
    private Integer completedCourses;
}
