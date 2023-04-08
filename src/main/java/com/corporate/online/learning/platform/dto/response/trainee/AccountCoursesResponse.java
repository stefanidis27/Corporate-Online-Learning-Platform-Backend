package com.corporate.online.learning.platform.dto.response.trainee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCoursesResponse {

    private Long id;
    private String name;
    private String category;
    private Integer numberOfAssignments;
    private Integer completedAssignments;
}
