package org.acme.exception;

public class CustomerNotFoundException extends ResourceNotFoundException {

    public CustomerNotFoundException(Long id) {
        super("CUSTOMER_NOT_FOUND", "Customer " + id + " was not found");
    }
}
