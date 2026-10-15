package org.acme.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 20) String document,
        @NotBlank @Email @Size(max = 160) String email
) {
}
