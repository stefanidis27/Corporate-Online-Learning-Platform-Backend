package com.corporate.online.learning.platform.exception.path;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PathCompletionStatsException extends RuntimeException {

    private String message;
}
