package com.gateway.payment.controller;

import com.gateway.payment.dto.account.BalanceResponse;
import com.gateway.payment.dto.account.DepositRequest;
import com.gateway.payment.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/balance")
    @PreAuthorize("hasAnyRole('CLIENT', 'MERCHANT')")
    public ResponseEntity<BalanceResponse> getBalance() {
        BalanceResponse response = accountService.getBalance();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    @PreAuthorize("hasAnyRole('CLIENT', 'MERCHANT')")
    public ResponseEntity<Void> deposit(@Valid @RequestBody DepositRequest request) {
        accountService.deposit(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}