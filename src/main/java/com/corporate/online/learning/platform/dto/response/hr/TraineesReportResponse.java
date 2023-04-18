package com.corporate.online.learning.platform.dto.response.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineesReportResponse {

    private String name;
    private String email;
    private String department;
    private String position;
    private String seniority;
    private Float progressLevel;
    private String enrollmentDate;
    private Float pathProgressLevel;
}
