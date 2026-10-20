package org.acme.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends BusinessRuleException {

    public InsufficientBalanceException(BigDecimal available, BigDecimal requested) {
        super("INSUFFICIENT_BALANCE",
                "Insufficient balance: available " + available + ", requested " + requested);
    }
}
