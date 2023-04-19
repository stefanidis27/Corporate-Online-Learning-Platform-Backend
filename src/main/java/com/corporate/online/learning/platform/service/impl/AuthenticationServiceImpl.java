package com.corporate.online.learning.platform.service.impl;

import com.corporate.online.learning.platform.config.ApplicationConfig;
import com.corporate.online.learning.platform.dto.request.auth.AuthenticationRequest;
import com.corporate.online.learning.platform.dto.request.auth.ChangeCredentialsRequest;
import com.corporate.online.learning.platform.dto.request.auth.CreateAccountRequest;
import com.corporate.online.learning.platform.dto.request.auth.ForgotPasswordRequest;
import com.corporate.online.learning.platform.dto.response.auth.AuthenticationResponse;
import com.corporate.online.learning.platform.dto.response.auth.ChangeCredentialsResponse;
import com.corporate.online.learning.platform.exception.account.*;
import com.corporate.online.learning.platform.model.account.Account;
import com.corporate.online.learning.platform.model.account.AccountDetails;
import com.corporate.online.learning.platform.model.account.Role;
import com.corporate.online.learning.platform.model.account.Token;
import com.corporate.online.learning.platform.repository.account.AccountDetailsRepository;
import com.corporate.online.learning.platform.repository.account.AccountRepository;
import com.corporate.online.learning.platform.repository.account.TokenRepository;
import com.corporate.online.learning.platform.service.AuthenticationService;
import com.corporate.online.learning.platform.service.EmailService;
import com.corporate.online.learning.platform.service.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AccountRepository accountRepository;
    private final AccountDetailsRepository accountDetailsRepository;
    private final ApplicationConfig applicationConfig;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public void createAccount(CreateAccountRequest request) {
        var accountDetails = AccountDetails.builder()
                .name(request.getName())
                .department(request.getDepartment())
                .position(request.getPosition())
                .seniority(request.getSeniority())
                .assignmentCompletionStats(new ArrayList<>())
                .courseCompletionStats(new ArrayList<>())
                .pathCompletionStats(new ArrayList<>())
                .taughtCourses(new ArrayList<>())
                .createdPaths(new ArrayList<>())
                .build();

        String password = generateRandomPassword();
        var account = Account.builder()
                .accountDetails(accountDetails)
                .email(request.getEmail())
                .password(passwordEncoder.encode(password))
                .role(Role.valueOf(request.getRole()))
                .locked(Boolean.FALSE)
                .failedLoginAttempts(0)
                .build();
        accountDetails.setAccount(account);
        account.setAccountDetails(accountDetails);
        try {
            accountDetailsRepository.save(accountDetails);
        } catch (DataAccessException e) {
            throw new AccountUniqueEmailException("[Account Creation Error] Account details could not be created.");
        }
        Account savedAccount;
        try {
            savedAccount = accountRepository.save(account);
        } catch (DataAccessException e) {
            throw new AccountUniqueEmailException("[Account Creation Error] Account could not be created.");
        }

        String jwtToken = jwtService.generateToken(account);
        saveAccountToken(savedAccount, jwtToken);
        emailService.sendEmailAccountCreationConfirmation(request.getEmail(), password);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AccountNotFoundException("[Authentication Error] No account with email "
                        + request.getEmail() + " found."));
        boolean accountLocked = checkAccountStillLocked(account);
        if (accountLocked) {
            throw new AccountLockedException("[Authentication Error] Account with email " + request.getEmail()
                    + " is currently locked for " + applicationConfig.getAccountLockTime() + " minutes.");
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()));

        var jwtToken= jwtService.generateToken(account);
        revokeAllAccountTokens(account);
        saveAccountToken(account, jwtToken);

        return AuthenticationResponse.builder()
                .id(account.getId())
                .role(account.getRole())
                .token(jwtToken)
                .build();
    }

    @Override
    public void resetForgottenPassword(ForgotPasswordRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AccountNotFoundException("[Password Change Error] No account with email "
                        + request.getEmail() + " found."));
        String newPassword = generateRandomPassword();

        account.setPassword(passwordEncoder.encode(newPassword));
        try {
            accountRepository.save(account);
        } catch (DataAccessException e) {
            throw new AccountException("[Password Change Error] Account with id " + account.getId()
                    + " could not be updated with the new password.");
        }

        emailService.sendEmailResetPasswordConfirmation(request.getEmail(), newPassword);
    }

    @Override
    public ChangeCredentialsResponse changeCredentials(Long id, ChangeCredentialsRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("[Credentials Change Error] No account with id " + id
                        + " found."));
        String oldEmail = account.getEmail();
        account.setEmail(ObjectUtils.isEmpty(request.getEmail()) ? account.getEmail() : request.getEmail());
        if (!ObjectUtils.isEmpty(request.getPassword())) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        try {
            accountRepository.save(account);
        } catch (DataAccessException e) {
            throw new AccountUniqueEmailException("[Credentials Change Error] Account with id " + id
                    + " could not be updated with the new credentials.");
        }

        var jwtToken= jwtService.generateToken(account);
        revokeAllAccountTokens(account);
        saveAccountToken(account, jwtToken);
        emailService.sendEmailCredentialsChangeConfirmation(
                oldEmail,
                request.getEmail(),
                request.getPassword());

        return ChangeCredentialsResponse.builder().token(jwtToken).build();
    }

    @Override
    @Transactional
    public void handleFailedLoginAttempt(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("[Handle Failed Login Error] No account with email "
                        + email + " found."));
        account.setFailedLoginAttempts(account.getFailedLoginAttempts() + 1);

        if (account.getFailedLoginAttempts() >= applicationConfig.getMaxLoginAttemptsAllowed() + 1) {
            account.setLocked(Boolean.TRUE);
            Date date = new Date(System.currentTimeMillis());
            account.setLockTimestamp(new Timestamp(date.getTime()));

            try {
                accountRepository.save(account);
            } catch (DataAccessException e) {
                throw new AccountException("[Handle Failed Login Error] No account with id "
                        + account.getId() + " could be updated.");
            }
            emailService.sendEmailAccountLockedConfirmation(email);
        }
    }

    @Transactional
    private boolean checkAccountStillLocked(Account account) {
        if (!ObjectUtils.isEmpty(account.getLockTimestamp())) {
            Date date = new Date(System.currentTimeMillis());
            Timestamp timestampInstant = new Timestamp(date.getTime());
            long millisElapsed = timestampInstant.getTime() - account.getLockTimestamp().getTime();

            if (account.getLocked().equals(Boolean.TRUE) && millisElapsed
                    > Duration.ofMinutes(applicationConfig.getAccountLockTime()).toMillis()) {
                account.setLocked(Boolean.FALSE);
                account.setFailedLoginAttempts(0);
                try {
                    accountRepository.save(account);
                } catch (DataAccessException e) {
                    throw new AccountException("[Authentication Error] Account with id " + account.getId()
                            + " could not be updated with the new failed login attempts.");
                }

                return false;
            } else return account.getLocked().equals(Boolean.TRUE);
        }

        return false;
    }

    private void revokeAllAccountTokens(Account account) {
        var validTokens = tokenRepository.findAllValidTokensByAccountId(account.getId());
        if (!CollectionUtils.isEmpty(validTokens)) {
            validTokens.forEach(token -> token.setExpired(true));

            try {
                tokenRepository.saveAll(validTokens);
            } catch (DataAccessException e) {
                throw new TokenException("[Token Revocation Error] Tokens could not be revoked for the account with id "
                        + account.getId() + ".");
            }
        }
    }

    private void saveAccountToken(Account savedAccount, String jwtToken) {
        var token = Token.builder()
                .account(savedAccount)
                .token(jwtToken)
                .expired(false)
                .build();

        try {
            tokenRepository.save(token);
        } catch (DataAccessException e) {
            throw new TokenException("[Token Revocation Error] Token " + jwtToken
                    + " could not be revoked for the account with id " + savedAccount.getId() + ".");
        }
    }

    private String generateRandomPassword() {
        int leftLimit = '0';
        int rightLimit = 'z';
        int targetStringLength = applicationConfig.getNewRandomPasswordLimit();
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= '9' || i >= 'A') && (i <= 'Z' || i >= 'a'))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
