package com.gateway.payment.repository;

import com.gateway.payment.domain.entity.Transaction;
import com.gateway.payment.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByPayerAccountOrMerchantAccount(Account payer, Account merchant);
    List<Transaction> findByPayerAccount(Account payer);
    List<Transaction> findByMerchantAccount(Account merchant);
}