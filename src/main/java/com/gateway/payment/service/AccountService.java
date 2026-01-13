package com.gateway.payment.service;

import com.gateway.payment.domain.entity.Account;
import com.gateway.payment.domain.entity.Transaction;
import com.gateway.payment.domain.entity.User;
import com.gateway.payment.domain.enums.TransactionStatus;
import com.gateway.payment.dto.account.BalanceResponse;
import com.gateway.payment.dto.account.DepositRequest;
import com.gateway.payment.exception.BusinessException;
import com.gateway.payment.repository.AccountRepository;
import com.gateway.payment.repository.TransactionRepository;
import com.gateway.payment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public BalanceResponse getBalance() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Conta não encontrada"));

        return new BalanceResponse(account.getBalance(), user.getName());
    }

    @Transactional
    public void deposit(DepositRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Conta não encontrada"));

        // Validação do valor
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Valor do depósito deve ser positivo");
        }

        // Adiciona o saldo
        account.credit(request.getAmount());
        accountRepository.save(account);

        // Cria uma transação de depósito (opcional)
        Transaction transaction = Transaction.builder()
                .payerAccount(account)  // Conta do próprio usuário
                .merchantAccount(account)  // Mesma conta (depósito)
                .amount(request.getAmount())
                .status(TransactionStatus.APPROVED)
                .description(request.getDescription() != null ?
                        request.getDescription() : "Depósito na conta")
                .build();

        transactionRepository.save(transaction);
    }
}