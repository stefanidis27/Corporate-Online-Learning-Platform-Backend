package com.corporate.online.learning.platform.dto.response.auth;

import com.corporate.online.learning.platform.model.account.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {

    private Long id;
    private Role role;
    private String token;
}
