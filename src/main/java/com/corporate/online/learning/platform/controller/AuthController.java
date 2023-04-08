package com.corporate.online.learning.platform.controller;

import com.corporate.online.learning.platform.dto.request.auth.AuthenticationRequest;
import com.corporate.online.learning.platform.dto.request.auth.ForgotPasswordRequest;
import com.corporate.online.learning.platform.dto.response.auth.AuthenticationResponse;
import com.corporate.online.learning.platform.dto.response.auth.FailedLoginStatusResponse;
import com.corporate.online.learning.platform.exception.ErrorMessage;
import com.corporate.online.learning.platform.exception.account.AccountDeletionException;
import com.corporate.online.learning.platform.exception.account.AccountDetailsNotFoundException;
import com.corporate.online.learning.platform.exception.account.AccountLockedException;
import com.corporate.online.learning.platform.exception.account.TokenException;
import com.corporate.online.learning.platform.service.impl.AuthenticationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationServiceImpl authService;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/recover-password")
    public ResponseEntity<Void> recoverPassword(@RequestBody ForgotPasswordRequest request) {
        authService.sendEmailToChangeForgottenPassword(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ExceptionHandler(AccountLockedException.class)
    public final ResponseEntity<ErrorMessage> handleAccountLockedException(AccountLockedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorMessage(e.getMessage()));
    }
}
