package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.dto.response.AccountResponse;
import org.acme.dto.response.CustomerResponse;
import org.acme.dto.response.TransactionResponse;
import org.acme.entity.Account;
import org.acme.entity.Customer;
import org.acme.entity.Transaction;

@ApplicationScoped
public class BankMapper {

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.id,
                customer.name,
                customer.document,
                customer.email,
                customer.createdAt
        );
    }

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.id,
                account.number,
                account.customer.id,
                account.currency,
                account.balance,
                account.active,
                account.openedAt
        );
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.id,
                transaction.sourceAccount == null ? null : transaction.sourceAccount.number,
                transaction.destinationAccount.number,
                transaction.amount,
                transaction.destinationAmount,
                transaction.sourceCurrency,
                transaction.destinationCurrency,
                transaction.exchangeRate,
                transaction.type,
                transaction.occurredAt,
                transaction.description
        );
    }
}
