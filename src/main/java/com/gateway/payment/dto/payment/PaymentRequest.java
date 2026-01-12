package com.gateway.payment.dto.payment;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @NotNull(message = "ID do merchant é obrigatório")
    private Long merchantId;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor mínimo é 0.01")
    @DecimalMax(value = "1000000.00", message = "Valor máximo é 1.000.000,00")
    private BigDecimal amount;

    private String description;
}