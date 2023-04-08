package com.corporate.online.learning.platform.exception.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReportRemoveTemporaryFileException extends RuntimeException {

    private String message;
}
