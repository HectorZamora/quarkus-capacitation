package org.acme.exception;

public class ExchangeRateUnavailableException extends RuntimeException {

    public ExchangeRateUnavailableException() {
        super("The exchange-rate provider is currently unavailable");
    }
}
