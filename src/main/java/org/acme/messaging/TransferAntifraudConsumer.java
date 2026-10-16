package org.acme.messaging;

import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.Alert;
import org.acme.repository.AlertRepository;
import org.acme.repository.TransactionRepository;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TransferAntifraudConsumer {

    private static final BigDecimal HIGH_VALUE_USD_THRESHOLD = new BigDecimal("5000.00");
    private static final int TRANSFER_BURST_THRESHOLD = 5;

    @Inject
    AlertRepository alertRepository;

    @Inject
    TransactionRepository transactionRepository;

    @Incoming("transfers-in")
    @Blocking
    @Transactional
    public void evaluate(TransferCompletedEvent event) {
        if (alertRepository.findByTransactionId(event.transactionId()).isPresent()) {
            return;
        }

        List<String> reasons = new ArrayList<>();
        if (event.amountInUsd() != null && event.amountInUsd().compareTo(HIGH_VALUE_USD_THRESHOLD) > 0) {
            reasons.add("Transfer exceeds USD 5000");
        }

        LocalDateTime occurredAt = event.occurredAt() == null ? LocalDateTime.now() : event.occurredAt();
        long transfersInWindow = transactionRepository.countTransfersInRange(
                event.sourceAccountNumber(), occurredAt.minusMinutes(5), occurredAt);
        if (transfersInWindow >= TRANSFER_BURST_THRESHOLD) {
            reasons.add("Five or more transfers from the same account within five minutes");
        }

        if (reasons.isEmpty()) {
            return;
        }

        Alert alert = new Alert();
        alert.transactionId = event.transactionId();
        alert.sourceAccountNumber = event.sourceAccountNumber();
        alert.destinationAccountNumber = event.destinationAccountNumber();
        alert.amount = event.amount();
        alert.currency = event.sourceCurrency();
        alert.amountInUsd = event.amountInUsd();
        alert.reason = String.join("; ", reasons);
        alert.createdAt = occurredAt;
        alertRepository.persist(alert);
    }
}
