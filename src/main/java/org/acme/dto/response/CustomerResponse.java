package org.acme.dto.response;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String name,
        String document,
        String email,
        LocalDateTime createdAt
) {
}
