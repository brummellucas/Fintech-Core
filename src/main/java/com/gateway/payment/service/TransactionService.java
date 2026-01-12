package com.gateway.payment.service;

import com.gateway.payment.domain.entity.Account;
import com.gateway.payment.domain.entity.Transaction;
import com.gateway.payment.domain.entity.User;
import com.gateway.payment.dto.transaction.TransactionResponse;
import com.gateway.payment.exception.BusinessException;
import com.gateway.payment.repository.AccountRepository;
import com.gateway.payment.repository.TransactionRepository;
import com.gateway.payment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public List<TransactionResponse> getUserTransactions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Conta não encontrada"));

        List<Transaction> transactions = transactionRepository
                .findByPayerAccountOrMerchantAccount(account, account);

        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .payerName(transaction.getPayerAccount().getUser().getName())
                .merchantName(transaction.getMerchantAccount().getUser().getName())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}