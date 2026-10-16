package org.acme.messaging;

import org.acme.entity.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferCompletedEvent(
        Long transactionId,
        String sourceAccountNumber,
        String destinationAccountNumber,
        BigDecimal amount,
        BigDecimal destinationAmount,
        Currency sourceCurrency,
        Currency destinationCurrency,
        BigDecimal amountInUsd,
        LocalDateTime occurredAt
) {
}
