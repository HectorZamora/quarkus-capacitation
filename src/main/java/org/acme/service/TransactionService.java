package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.request.DepositRequest;
import org.acme.dto.request.TransferRequest;
import org.acme.dto.response.TransactionResponse;
import org.acme.entity.Account;
import org.acme.entity.Transaction;
import org.acme.entity.TransactionType;
import org.acme.exception.AccountNotFoundException;
import org.acme.exception.BusinessRuleException;
import org.acme.exception.ConflictException;
import org.acme.exception.InsufficientBalanceException;
import org.acme.exception.TransactionNotFoundException;
import org.acme.mapper.BankMapper;
import org.acme.repository.AccountRepository;
import org.acme.repository.TransactionRepository;

@ApplicationScoped
public class TransactionService {

    @Inject
    AccountRepository accountRepository;

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    BankMapper mapper;

    @Transactional
    public TransactionResponse deposit(String accountNumber, DepositRequest request) {
        Account account = accountRepository.findByNumberForUpdate(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        ensureActive(account);

        account.balance = account.balance.add(request.amount());
        Transaction transaction = new Transaction();
        transaction.destinationAccount = account;
        transaction.amount = request.amount();
        transaction.destinationAmount = request.amount();
        transaction.sourceCurrency = account.currency;
        transaction.destinationCurrency = account.currency;
        transaction.type = TransactionType.DEPOSIT;
        transaction.description = request.description();
        transactionRepository.persist(transaction);
        return mapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse transfer(String sourceNumber, TransferRequest request) {
        String destinationNumber = request.destinationAccountNumber();
        if (sourceNumber.equals(destinationNumber)) {
            throw new ConflictException("SAME_SOURCE_AND_DESTINATION",
                    "Source and destination accounts must be different");
        }

        Account first = lockAccount(sourceNumber.compareTo(destinationNumber) < 0 ? sourceNumber : destinationNumber);
        Account second = lockAccount(sourceNumber.compareTo(destinationNumber) < 0 ? destinationNumber : sourceNumber);
        Account source = first.number.equals(sourceNumber) ? first : second;
        Account destination = first.number.equals(destinationNumber) ? first : second;
        ensureActive(source);
        ensureActive(destination);

        if (source.currency != destination.currency) {
            throw new BusinessRuleException("CURRENCY_CONVERSION_NOT_AVAILABLE",
                    "Transfers between different currencies are not available yet");
        }
        if (source.balance.compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException(source.balance, request.amount());
        }

        source.balance = source.balance.subtract(request.amount());
        destination.balance = destination.balance.add(request.amount());

        Transaction transaction = new Transaction();
        transaction.sourceAccount = source;
        transaction.destinationAccount = destination;
        transaction.amount = request.amount();
        transaction.destinationAmount = request.amount();
        transaction.sourceCurrency = source.currency;
        transaction.destinationCurrency = destination.currency;
        transaction.type = TransactionType.TRANSFER;
        transaction.description = request.description();
        transactionRepository.persist(transaction);
        return mapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse findById(Long id) {
        Transaction transaction = transactionRepository.findByIdOptional(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        return mapper.toResponse(transaction);
    }

    private Account lockAccount(String number) {
        return accountRepository.findByNumberForUpdate(number)
                .orElseThrow(() -> new AccountNotFoundException(number));
    }

    private void ensureActive(Account account) {
        if (!account.active) {
            throw new BusinessRuleException("ACCOUNT_INACTIVE", "Account '" + account.number + "' is inactive");
        }
    }
}
