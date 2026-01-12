package com.gateway.payment.dto.transaction;

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
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionStatus status;
    private String payerName;
    private String merchantName;
    private String description;
    private LocalDateTime createdAt;
}