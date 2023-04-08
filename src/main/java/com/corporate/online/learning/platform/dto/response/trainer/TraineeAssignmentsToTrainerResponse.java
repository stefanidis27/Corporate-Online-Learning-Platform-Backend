package com.corporate.online.learning.platform.dto.response.trainer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeAssignmentsToTrainerResponse {

    private Long id;
    private String text;
    private String comment;
}
