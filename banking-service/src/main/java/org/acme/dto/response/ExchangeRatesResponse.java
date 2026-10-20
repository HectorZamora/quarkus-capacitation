package org.acme.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

public record ExchangeRatesResponse(
        String result,
        @JsonProperty("base_code") String baseCode,
        Map<String, BigDecimal> rates
) {
}
