package com.gateway.payment.service;

import com.gateway.payment.domain.entity.Account;
import com.gateway.payment.domain.entity.Transaction;
import com.gateway.payment.domain.entity.User;
import com.gateway.payment.domain.enums.TransactionStatus;
import com.gateway.payment.dto.payment.PaymentRequest;
import com.gateway.payment.dto.payment.PaymentResponse;
import com.gateway.payment.exception.BusinessException;
import com.gateway.payment.exception.InsufficientBalanceException;
import com.gateway.payment.repository.AccountRepository;
import com.gateway.payment.repository.TransactionRepository;
import com.gateway.payment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processando pagamento de {} para merchant {}",
                request.getAmount(), request.getMerchantId());

        // 1. Obter usuário autenticado (payer)
        String payerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User payer = userRepository.findByEmail(payerEmail)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        Account payerAccount = accountRepository.findByUser(payer)
                .orElseThrow(() -> new BusinessException("Conta do pagador não encontrada"));

        // 2. Validar merchant
        User merchant = userRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new BusinessException("Merchant não encontrado"));

        Account merchantAccount = accountRepository.findByUser(merchant)
                .orElseThrow(() -> new BusinessException("Conta do merchant não encontrada"));

        // 3. Validar se o merchant realmente é um merchant
        if (!merchant.getRole().name().equals("MERCHANT")) {
            throw new BusinessException("Usuário destino não é um merchant");
        }

        // 4. Validar saldo (com lock pessimista para evitar concorrência)
        Account payerAccountLocked = accountRepository.findByIdWithLock(payerAccount.getId())
                .orElseThrow(() -> new BusinessException("Conta do pagador não encontrada"));

        if (payerAccountLocked.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente");
        }

        // 5. Criar transação com status PENDING
        Transaction transaction = Transaction.builder()
                .payerAccount(payerAccountLocked)
                .merchantAccount(merchantAccount)
                .amount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        try {
            // 6. Processar débito e crédito (ATÔMICO)
            payerAccountLocked.debit(request.getAmount());
            merchantAccount.credit(request.getAmount());

            // 7. Atualizar contas
            accountRepository.save(payerAccountLocked);
            accountRepository.save(merchantAccount);

            // 8. Atualizar status da transação
            savedTransaction.setStatus(TransactionStatus.APPROVED);
            transactionRepository.save(savedTransaction);

            log.info("Pagamento {} aprovado com sucesso", savedTransaction.getId());

            return PaymentResponse.builder()
                    .transactionId(savedTransaction.getId())
                    .amount(savedTransaction.getAmount())
                    .status(savedTransaction.getStatus())
                    .merchantName(merchant.getName())
                    .description(savedTransaction.getDescription())
                    .createdAt(savedTransaction.getCreatedAt())
                    .build();

        } catch (Exception e) {
            // 9. Em caso de erro, marcar como FAILED
            savedTransaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(savedTransaction);

            log.error("Erro ao processar pagamento: {}", e.getMessage());
            throw new BusinessException("Falha ao processar pagamento: " + e.getMessage());
        }
    }
}