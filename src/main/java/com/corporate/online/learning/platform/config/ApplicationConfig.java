package com.corporate.online.learning.platform.config;

import com.corporate.online.learning.platform.repository.account.AccountRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final AccountRepository accountRepository;

    @Value("${website.description}")
    private String websiteDescription;

    @Value("${page.size}")
    private Integer fixedPageSize;

    @Value("${max.failed.login.attempts.allowed}")
    private Integer maxLoginAttemptsAllowed;

    @Value("${new.random.password.limit}")
    private Integer newRandomPasswordLimit;

    @Value("${account.expiration.time}")
    private Long accountExpirationTime;

    @Value("${account.lock.time}")
    private Long accountLockTime;

    @Value("${spring.mail.username}")
    private String emailSenderAddress;

    @Value("${authorization.type}")
    private String authorizationType;

    @Value("${date.format}")
    private String dateFormat;

    @Value("${temporary.file.name}")
    private String temporaryFileName;

    @Value("${security.key}")
    private String securityKey;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> accountRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
