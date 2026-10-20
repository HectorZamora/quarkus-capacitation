package org.acme.exception;

public class TransactionNotFoundException extends ResourceNotFoundException {

    public TransactionNotFoundException(Long id) {
        super("TRANSACTION_NOT_FOUND", "Transaction " + id + " was not found");
    }
}
