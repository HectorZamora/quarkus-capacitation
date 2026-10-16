package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.acme.dto.request.DepositRequest;
import org.acme.dto.request.TransferRequest;
import org.acme.dto.response.TransactionResponse;
import org.acme.entity.Account;
import org.acme.entity.Currency;
import org.acme.entity.Transaction;
import org.acme.entity.TransactionType;
import org.acme.exception.AccountNotFoundException;
import org.acme.exception.BusinessRuleException;
import org.acme.exception.ConflictException;
import org.acme.exception.ExchangeRateUnavailableException;
import org.acme.exception.InsufficientBalanceException;
import org.acme.exception.TransactionNotFoundException;
import org.acme.mapper.BankMapper;
import org.acme.messaging.TransferCompletedEvent;
import org.acme.repository.AccountRepository;
import org.acme.repository.TransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@ApplicationScoped
public class TransactionService {

    private static final Logger LOG = Logger.getLogger(TransactionService.class);

    @Inject
    AccountRepository accountRepository;

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    BankMapper mapper;

    @Inject
    ExchangeRateService exchangeRateService;

    @Inject
    Event<TransferCompletedEvent> transferEvents;

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

        Account sourceSnapshot = accountRepository.findByNumber(sourceNumber)
                .orElseThrow(() -> new AccountNotFoundException(sourceNumber));
        Account destinationSnapshot = accountRepository.findByNumber(destinationNumber)
                .orElseThrow(() -> new AccountNotFoundException(destinationNumber));
        ensureActive(sourceSnapshot);
        ensureActive(destinationSnapshot);
        if (sourceSnapshot.balance.compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException(sourceSnapshot.balance, request.amount());
        }

        BigDecimal exchangeRate = null;
        BigDecimal destinationAmount = request.amount();
        BigDecimal amountInUsd = sourceSnapshot.currency == Currency.USD ? request.amount() : null;
        if (sourceSnapshot.currency != destinationSnapshot.currency) {
            exchangeRate = exchangeRateService.getRate(sourceSnapshot.currency, destinationSnapshot.currency);
            destinationAmount = request.amount().multiply(exchangeRate).setScale(4, RoundingMode.HALF_UP);
            if (destinationAmount.signum() <= 0) {
                throw new BusinessRuleException("CONVERTED_AMOUNT_TOO_SMALL",
                        "The converted amount is below the supported precision");
            }
            if (destinationSnapshot.currency == Currency.USD) {
                amountInUsd = destinationAmount;
            }
        } else if (sourceSnapshot.currency == Currency.PEN) {
            try {
                BigDecimal penToUsdRate = exchangeRateService.getRate(Currency.PEN, Currency.USD);
                amountInUsd = request.amount().multiply(penToUsdRate).setScale(4, RoundingMode.HALF_UP);
            } catch (ExchangeRateUnavailableException exception) {
                LOG.warn("Skipping USD-equivalent antifraud check because the exchange-rate provider is unavailable");
            }
        }

        Account first = lockAccount(sourceNumber.compareTo(destinationNumber) < 0 ? sourceNumber : destinationNumber);
        Account second = lockAccount(sourceNumber.compareTo(destinationNumber) < 0 ? destinationNumber : sourceNumber);
        Account source = first.number.equals(sourceNumber) ? first : second;
        Account destination = first.number.equals(destinationNumber) ? first : second;
        ensureActive(source);
        ensureActive(destination);
        if (source.currency != sourceSnapshot.currency || destination.currency != destinationSnapshot.currency) {
            throw new BusinessRuleException("ACCOUNT_CURRENCY_CHANGED",
                    "An account currency changed while processing the transfer");
        }
        if (source.balance.compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException(source.balance, request.amount());
        }

        source.balance = source.balance.subtract(request.amount());
        destination.balance = destination.balance.add(destinationAmount);

        Transaction transaction = new Transaction();
        transaction.sourceAccount = source;
        transaction.destinationAccount = destination;
        transaction.amount = request.amount();
        transaction.destinationAmount = destinationAmount;
        transaction.sourceCurrency = source.currency;
        transaction.destinationCurrency = destination.currency;
        transaction.exchangeRate = exchangeRate;
        transaction.type = TransactionType.TRANSFER;
        transaction.description = request.description();
        transactionRepository.persist(transaction);

        transferEvents.fire(new TransferCompletedEvent(
                transaction.id,
                source.number,
                destination.number,
                request.amount(),
                destinationAmount,
                source.currency,
                destination.currency,
                amountInUsd,
                transaction.occurredAt
        ));
        return mapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse findById(Long id) {
        Transaction transaction = transactionRepository.findByIdOptional(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        return mapper.toResponse(transaction);
    }

    @Transactional
    public List<TransactionResponse> listAll() {
        return transactionRepository.list("order by occurredAt desc").stream()
                .map(mapper::toResponse)
                .toList();
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
