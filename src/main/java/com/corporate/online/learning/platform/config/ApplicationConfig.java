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

    @Value("${mail.subject.reset.password}")
    private String emailSubjectResetPassword;

    @Value("${mail.body.reset.password}")
    private String emailBodyResetPassword;

    @Value("${mail.subject.create.account}")
    private String emailSubjectAccountCreation;

    @Value("${mail.body.create.account}")
    private String emailBodyAccountCreation;

    @Value("${mail.subject.change.credentials}")
    private String emailSubjectCredentialsChange;

    @Value("${mail.body.change.credentials}")
    private String emailBodyCredentialsChange;

    @Value("${mail.body.change.credentials.password}")
    private String emailBodyCredentialsChangePasswordMessage;

    @Value("${mail.body.change.credentials.old.email}")
    private String emailBodyCredentialsChangeOldEmailMessage;

    @Value("${mail.body.change.credentials.new.email}")
    private String emailBodyCredentialsChangeNewEmailMessage;

    @Value("${mail.subject.lock.account}")
    private String emailSubjectAccountLocked;

    @Value("${mail.body.lock.account}")
    private String emailBodyAccountLocked;

    @Value("${mail.subject.delete.account}")
    private String emailSubjectAccountDeleted;

    @Value("${mail.body.delete.account}")
    private String emailBodyAccountDeleted;

    @Value("${mail.subject.create.course}")
    private String emailSubjectCourseCreated;

    @Value("${mail.body.create.course}")
    private String emailBodyCourseCreated;

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
