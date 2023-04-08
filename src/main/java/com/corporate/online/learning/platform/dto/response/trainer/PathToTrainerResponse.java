package com.corporate.online.learning.platform.dto.response.trainer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathToTrainerResponse {

    private Long id;
    private String name;
    private String category;
    private Long currentEnrollments;
    private Integer noCourses;
}
