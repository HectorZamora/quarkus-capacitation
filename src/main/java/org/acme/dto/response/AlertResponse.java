package org.acme.dto.response;

import org.acme.entity.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        Long transactionId,
        String sourceAccountNumber,
        String destinationAccountNumber,
        BigDecimal amount,
        Currency currency,
        BigDecimal amountInUsd,
        String reason,
        LocalDateTime createdAt,
        boolean reviewed
) {
}
