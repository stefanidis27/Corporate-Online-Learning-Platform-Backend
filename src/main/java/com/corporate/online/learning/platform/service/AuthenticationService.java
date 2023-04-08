package com.corporate.online.learning.platform.service;

import com.corporate.online.learning.platform.dto.request.auth.AuthenticationRequest;
import com.corporate.online.learning.platform.dto.request.auth.ChangeCredentialsRequest;
import com.corporate.online.learning.platform.dto.request.auth.CreateAccountRequest;
import com.corporate.online.learning.platform.dto.request.auth.ForgotPasswordRequest;
import com.corporate.online.learning.platform.dto.response.auth.AuthenticationResponse;
import com.corporate.online.learning.platform.dto.response.auth.ChangeCredentialsResponse;
import com.corporate.online.learning.platform.dto.response.auth.FailedLoginStatusResponse;

public interface AuthenticationService {

    void createAccount(CreateAccountRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    void sendEmailToChangeForgottenPassword(ForgotPasswordRequest request);

    ChangeCredentialsResponse changeCredentials(Long id, ChangeCredentialsRequest request);
}
