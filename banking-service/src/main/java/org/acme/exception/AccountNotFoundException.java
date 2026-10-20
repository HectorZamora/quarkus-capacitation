package org.acme.exception;

public class AccountNotFoundException extends ResourceNotFoundException {

    public AccountNotFoundException(String number) {
        super("ACCOUNT_NOT_FOUND", "Account '" + number + "' was not found");
    }
}
