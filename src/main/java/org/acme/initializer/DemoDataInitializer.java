package org.acme.initializer;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.Account;
import org.acme.entity.Currency;
import org.acme.entity.Customer;
import org.acme.repository.AccountRepository;
import org.acme.repository.CustomerRepository;

import java.math.BigDecimal;

@ApplicationScoped
@IfBuildProfile(anyOf = {"dev", "test"})
public class DemoDataInitializer {

    @Inject
    CustomerRepository customerRepository;

    @Inject
    AccountRepository accountRepository;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        Customer firstCustomer = ensureCustomer("Ana Rojas", "45678912", "ana@minibanco.test");
        Customer secondCustomer = ensureCustomer("Luis Perez", "87654321", "luis@minibanco.test");

        ensureAccount("PEN-000001", firstCustomer, Currency.PEN, "10000.00");
        ensureAccount("USD-000001", firstCustomer, Currency.USD, "6000.00");
        ensureAccount("PEN-000002", secondCustomer, Currency.PEN, "2500.00");
    }

    private Customer ensureCustomer(String name, String document, String email) {
        return customerRepository.findByDocument(document).orElseGet(() -> {
            Customer customer = new Customer();
            customer.name = name;
            customer.document = document;
            customer.email = email;
            customerRepository.persist(customer);
            return customer;
        });
    }

    private void ensureAccount(String number, Customer customer, Currency currency, String balance) {
        if (accountRepository.findByNumber(number).isPresent()) {
            return;
        }

        Account account = new Account();
        account.number = number;
        account.customer = customer;
        account.currency = currency;
        account.balance = new BigDecimal(balance);
        accountRepository.persist(account);
    }
}
