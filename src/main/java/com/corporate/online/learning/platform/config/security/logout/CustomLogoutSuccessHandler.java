package com.corporate.online.learning.platform.config.security.logout;

import com.corporate.online.learning.platform.config.ApplicationConfig;
import com.corporate.online.learning.platform.exception.account.AccountException;
import com.corporate.online.learning.platform.exception.account.TokenNotFoundException;
import com.corporate.online.learning.platform.model.account.Account;
import com.corporate.online.learning.platform.repository.account.AccountRepository;
import com.corporate.online.learning.platform.repository.account.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final TokenRepository tokenRepository;
    private final ApplicationConfig applicationConfig;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (ObjectUtils.isEmpty(authorizationHeader) || !authorizationHeader.startsWith(
                applicationConfig.getAuthorizationType() + 1)) {
            return;
        }

        final String jwtToken = authorizationHeader.substring(applicationConfig.getAuthorizationType().length() + 1);
        var storedToken = tokenRepository.findByToken(jwtToken)
                .orElseThrow(() -> new TokenNotFoundException("[Logout Error] Token with content " + jwtToken
                        + " could not be found."));

        if (!ObjectUtils.isEmpty(storedToken)) {
            Account account = storedToken.getAccount();
            account.setFailedLoginAttempts(0);
            try {
                accountRepository.save(account);
            } catch (DataAccessException e) {
                throw new AccountException("[Logout Success Error] Account with id " + account.getId()
                        + " could not be updated with the new failed login attempts.");
            }
        }
    }
}
