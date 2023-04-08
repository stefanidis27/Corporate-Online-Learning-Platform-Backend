package com.corporate.online.learning.platform.config.security.logout;

import com.corporate.online.learning.platform.config.ApplicationConfig;
import com.corporate.online.learning.platform.exception.account.TokenException;
import com.corporate.online.learning.platform.exception.account.TokenNotFoundException;
import com.corporate.online.learning.platform.repository.account.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final TokenRepository tokenRepository;
    private final ApplicationConfig applicationConfig;

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (ObjectUtils.isEmpty(authorizationHeader) || !authorizationHeader.startsWith(
                applicationConfig.getAuthorizationType() + " ")) {
            return;
        }

        final String jwtToken = authorizationHeader.substring(applicationConfig.getAuthorizationType().length() + 1);
        var storedToken = tokenRepository.findByToken(jwtToken)
                .orElseThrow(() -> new TokenNotFoundException("[Logout Error] Token with content " + jwtToken
                        + " could not be found."));

        if (!ObjectUtils.isEmpty(storedToken)) {
            storedToken.setExpired(true);
            try {
                tokenRepository.save(storedToken);
            } catch (DataAccessException e) {
                throw new TokenException("[Logout Error] Token with id " + storedToken.getId()
                        + " could not be set to expired.");
            }
        }

        SecurityContextHolder.clearContext();
    }
}
