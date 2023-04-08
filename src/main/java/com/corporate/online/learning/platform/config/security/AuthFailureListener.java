package com.corporate.online.learning.platform.config.security;

import com.corporate.online.learning.platform.service.impl.AuthenticationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final AuthenticationServiceImpl authenticationService;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
        final String source = e.getSource().toString();
        final String accountEmail = source.substring(source.indexOf('=') + 1, source.indexOf(','));

        authenticationService.handleFailedLoginAttempt(accountEmail);
    }
}
