package com.gateway.payment.dto.payment;

import com.gateway.payment.domain.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long transactionId;
    private BigDecimal amount;
    private TransactionStatus status;
    private String merchantName;
    private String description;
    private LocalDateTime createdAt;
}