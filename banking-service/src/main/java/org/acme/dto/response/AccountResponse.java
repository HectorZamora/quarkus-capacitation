package org.acme.dto.response;

import org.acme.entity.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String number,
        Long customerId,
        Currency currency,
        BigDecimal balance,
        boolean active,
        LocalDateTime openedAt
) {
}
