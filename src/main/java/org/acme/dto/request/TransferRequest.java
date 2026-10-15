package org.acme.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9-]+") @Size(max = 24) String destinationAccountNumber,
        @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal amount,
        @Size(max = 250) String description
) {
}
