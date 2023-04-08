package com.corporate.online.learning.platform.controller;

import com.corporate.online.learning.platform.dto.request.auth.CreateAccountRequest;
import com.corporate.online.learning.platform.dto.response.common.AllAccountsResponse;
import com.corporate.online.learning.platform.exception.ErrorMessage;
import com.corporate.online.learning.platform.exception.account.AccountUniqueEmailException;
import com.corporate.online.learning.platform.exception.account.AccountDeletionException;
import com.corporate.online.learning.platform.service.AdminService;
import com.corporate.online.learning.platform.service.AuthenticationService;
import com.corporate.online.learning.platform.service.CommonService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AuthenticationService authenticationService;
    private final AdminService adminService;
    private final CommonService commonService;

    @PostMapping("/create-account")
    public ResponseEntity<Void> createNewAccount(@RequestBody CreateAccountRequest request) {
        authenticationService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/delete-account/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long accountId) {
        adminService.deleteAccount(accountId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{adminId}/get-accounts/{pageNo}")
    public ResponseEntity<List<AllAccountsResponse>> showAccounts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String seniority,
            @RequestParam(required = false) String email,
            @PathVariable Long adminId,
            @PathVariable Integer pageNo) {
        return ResponseEntity.ok(commonService.showAllAccounts(
                name, department, position,
                seniority, email, adminId, pageNo));
    }

    @ExceptionHandler(AccountDeletionException.class)
    public final ResponseEntity<ErrorMessage> handleAccountDeletionException(AccountDeletionException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }
}
