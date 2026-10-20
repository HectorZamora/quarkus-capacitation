package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.ProcessedTransfer;

import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class ProcessedTransferRepository implements PanacheRepository<ProcessedTransfer> {

    public Optional<ProcessedTransfer> findByTransactionId(Long transactionId) {
        return find("transactionId", transactionId).firstResultOptional();
    }

    public long countTransfersInRange(String sourceAccountNumber, LocalDateTime from, LocalDateTime to) {
        return count("sourceAccountNumber = ?1 and occurredAt >= ?2 and occurredAt <= ?3",
                sourceAccountNumber, from, to);
    }
}
