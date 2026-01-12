package com.gateway.payment.service;

import com.gateway.payment.domain.entity.Account;
import com.gateway.payment.domain.entity.User;
import com.gateway.payment.dto.account.BalanceResponse;
import com.gateway.payment.exception.BusinessException;
import com.gateway.payment.repository.AccountRepository;
import com.gateway.payment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public BalanceResponse getBalance() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Conta não encontrada"));

        return new BalanceResponse(account.getBalance(), user.getName());
    }
}