package com.corporate.online.learning.platform.dto.request.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    private String name;
    private String department;
    private String position;
    private String seniority;
    private String email;
    private String role;
}
