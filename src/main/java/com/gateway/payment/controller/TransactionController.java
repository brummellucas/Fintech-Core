package com.gateway.payment.controller;

import com.gateway.payment.dto.transaction.TransactionResponse;
import com.gateway.payment.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'MERCHANT')")
    public ResponseEntity<List<TransactionResponse>> getUserTransactions() {
        List<TransactionResponse> transactions = transactionService.getUserTransactions();
        return ResponseEntity.ok(transactions);
    }
}