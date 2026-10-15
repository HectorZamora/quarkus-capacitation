package org.acme.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.acme.entity.Currency;

public record OpenAccountRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9-]+") @Size(max = 24) String number,
        @NotNull @Positive Long customerId,
        @NotNull Currency currency
) {
}
