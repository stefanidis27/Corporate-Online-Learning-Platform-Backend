package com.corporate.online.learning.platform.dto.response.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalInfoResponse {

    private String name;
    private String department;
    private String position;
    private String seniority;
}
