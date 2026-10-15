package org.acme.dto.response;

import org.acme.entity.Currency;
import org.acme.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String sourceAccountNumber,
        String destinationAccountNumber,
        BigDecimal amount,
        BigDecimal destinationAmount,
        Currency sourceCurrency,
        Currency destinationCurrency,
        BigDecimal exchangeRate,
        TransactionType type,
        LocalDateTime occurredAt,
        String description
) {
}
