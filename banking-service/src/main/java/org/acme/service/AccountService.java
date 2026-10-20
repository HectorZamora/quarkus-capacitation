package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.request.OpenAccountRequest;
import org.acme.dto.response.AccountResponse;
import org.acme.entity.Account;
import org.acme.entity.Customer;
import org.acme.exception.AccountNotFoundException;
import org.acme.exception.BusinessRuleException;
import org.acme.exception.ConflictException;
import org.acme.exception.CustomerNotFoundException;
import org.acme.mapper.BankMapper;
import org.acme.repository.AccountRepository;
import org.acme.repository.CustomerRepository;

import java.math.BigDecimal;

@ApplicationScoped
public class AccountService {

    @Inject
    AccountRepository accountRepository;

    @Inject
    CustomerRepository customerRepository;

    @Inject
    BankMapper mapper;

    @Transactional
    public AccountResponse open(OpenAccountRequest request) {
        if (accountRepository.findByNumber(request.number()).isPresent()) {
            throw new ConflictException("ACCOUNT_NUMBER_ALREADY_EXISTS", "An account with this number already exists");
        }

        Customer customer = customerRepository.findByIdOptional(request.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.customerId()));
        Account account = new Account();
        account.number = request.number();
        account.customer = customer;
        account.currency = request.currency();
        account.balance = BigDecimal.ZERO;
        accountRepository.persist(account);
        return mapper.toResponse(account);
    }

    @Transactional
    public AccountResponse findByNumber(String number) {
        Account account = accountRepository.findByNumber(number)
                .orElseThrow(() -> new AccountNotFoundException(number));
        return mapper.toResponse(account);
    }

    @Transactional
    public void close(String number) {
        Account account = accountRepository.findByNumberForUpdate(number)
                .orElseThrow(() -> new AccountNotFoundException(number));
        if (account.balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessRuleException("ACCOUNT_BALANCE_NOT_ZERO",
                    "An account can only be closed when its balance is zero");
        }
        account.active = false;
    }
}
