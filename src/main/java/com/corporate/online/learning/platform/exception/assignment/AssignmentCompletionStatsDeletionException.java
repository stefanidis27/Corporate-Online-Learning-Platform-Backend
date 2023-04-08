package com.corporate.online.learning.platform.exception.assignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AssignmentCompletionStatsDeletionException extends RuntimeException {

    private String message;
}
