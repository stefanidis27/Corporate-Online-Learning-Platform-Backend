package com.corporate.online.learning.platform.exception.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CourseCompletionStatsNotFoundException extends RuntimeException {

    private String message;
}
