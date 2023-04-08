package com.corporate.online.learning.platform.dto.response.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainersToHRResponse {

    private Long id;
    private String name;
    private String email;
    private Integer noTaughtCourses;
    private Long noCurrentTrainees;
}
