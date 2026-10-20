package org.acme.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DepositRequest(
        @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal amount,
        @Size(max = 250) String description
) {
}
