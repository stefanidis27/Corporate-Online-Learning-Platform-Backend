package com.corporate.online.learning.platform.controller;

import com.corporate.online.learning.platform.dto.request.auth.ChangeCredentialsRequest;
import com.corporate.online.learning.platform.dto.response.auth.ChangeCredentialsResponse;
import com.corporate.online.learning.platform.service.CommonService;
import com.corporate.online.learning.platform.service.AuthenticationService;
import com.corporate.online.learning.platform.dto.response.common.PersonalInfoResponse;
import com.corporate.online.learning.platform.dto.response.common.WebsiteInfoResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/common")
public class CommonController {

    private final AuthenticationService authenticationService;
    private final CommonService commonService;

    @GetMapping("/{id}/personal-info")
    public ResponseEntity<PersonalInfoResponse> showPersonalInfo(@PathVariable Long id) {
        return ResponseEntity.ok(commonService.getPersonalInfo(id));
    }

    @PostMapping("/{id}/change-credentials")
    public ResponseEntity<ChangeCredentialsResponse> changeCredentials(
            @PathVariable Long id,
            @RequestBody ChangeCredentialsRequest request) {
        return ResponseEntity.ok(authenticationService.changeCredentials(id, request));
    }

    @GetMapping("/get-website-description")
    public ResponseEntity<WebsiteInfoResponse> showWebsiteDescription() {
        return ResponseEntity.ok(commonService.getWebsiteDescription());
    }
}
