package org.acme.repository;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.acme.entity.Account;
import org.acme.entity.Currency;
import org.acme.entity.Customer;
import org.acme.entity.Transaction;
import org.acme.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class PersistenceRepositoryTest {

    @Inject
    CustomerRepository customerRepository;

    @Inject
    AccountRepository accountRepository;

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    EntityManager entityManager;

    @Test
    void loadsDemoDataAndFindsAccountsByCustomer() {
        Customer customer = customerRepository.findByDocument("45678912").orElseThrow();

        assertTrue(customerRepository.findByEmail("ana@minibanco.test").isPresent());
        assertTrue(accountRepository.findByNumber("PEN-000001").isPresent());
        assertEquals(2, accountRepository.listByCustomer(customer.id).size());
        assertFalse(customerRepository.findByDocument("not-found").isPresent());
    }

    @Test
    @TestTransaction
    void findsTransactionsByAccountAndDateRange() {
        Account source = accountRepository.findByNumber("PEN-000001").orElseThrow();
        Account destination = accountRepository.findByNumber("PEN-000002").orElseThrow();
        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = new Transaction();
        transaction.sourceAccount = source;
        transaction.destinationAccount = destination;
        transaction.amount = new BigDecimal("125.50");
        transaction.destinationAmount = new BigDecimal("125.50");
        transaction.sourceCurrency = Currency.PEN;
        transaction.destinationCurrency = Currency.PEN;
        transaction.type = TransactionType.TRANSFER;
        transaction.occurredAt = now;
        transactionRepository.persistAndFlush(transaction);

        assertEquals("TRANSFER", entityManager.createNativeQuery(
                        "select transaction_type from transactions where id = :id")
                .setParameter("id", transaction.id)
                .getSingleResult());

        Transaction deposit = new Transaction();
        deposit.destinationAccount = destination;
        deposit.amount = new BigDecimal("50.00");
        deposit.destinationAmount = new BigDecimal("50.00");
        deposit.sourceCurrency = Currency.PEN;
        deposit.destinationCurrency = Currency.PEN;
        deposit.type = TransactionType.DEPOSIT;
        deposit.occurredAt = now;
        transactionRepository.persistAndFlush(deposit);

        assertEquals("DEPOSIT", entityManager.createNativeQuery(
                        "select transaction_type from transactions where id = :id")
                .setParameter("id", deposit.id)
                .getSingleResult());
        assertTrue(transactionRepository.listByDateRange(now.minusMinutes(1), now.plusMinutes(1))
                .stream().anyMatch(item -> item.id.equals(transaction.id)));
        assertTrue(transactionRepository.listByAccount(source.number)
                .stream().anyMatch(item -> item.id.equals(transaction.id)));
        assertEquals(1, transactionRepository.countRecentTransfers(source.number, now.minusMinutes(1)));
    }
}
