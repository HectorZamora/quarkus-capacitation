package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.client.ExchangeRateClient;
import org.acme.dto.response.ExchangeRatesResponse;
import org.acme.entity.Currency;
import org.acme.exception.ExchangeRateUnavailableException;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class ExchangeRateService {

    @Inject
    @RestClient
    ExchangeRateClient exchangeRateClient;

    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "fallbackExchangeRate")
    public BigDecimal getRate(Currency sourceCurrency, Currency destinationCurrency) {
        ExchangeRatesResponse response = exchangeRateClient.getLatestRates(sourceCurrency.name());
        if (response == null
                || !"success".equals(response.result())
                || !sourceCurrency.name().equals(response.baseCode())
                || response.rates() == null) {
            throw new IllegalStateException("The exchange-rate response is invalid");
        }

        BigDecimal rate = response.rates().get(destinationCurrency.name());
        if (rate == null || rate.signum() <= 0) {
            throw new IllegalStateException("The exchange-rate response has no valid target currency rate");
        }
        return rate;
    }

    public BigDecimal fallbackExchangeRate(Currency sourceCurrency, Currency destinationCurrency) {
        throw new ExchangeRateUnavailableException();
    }
}
