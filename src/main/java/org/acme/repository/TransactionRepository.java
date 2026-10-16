package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Transaction;
import org.acme.entity.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {

    public List<Transaction> listByDateRange(LocalDateTime from, LocalDateTime to) {
        return list("occurredAt >= ?1 and occurredAt <= ?2 order by occurredAt desc", from, to);
    }

    public List<Transaction> listByAccount(String accountNumber) {
        return list("sourceAccount.number = ?1 or destinationAccount.number = ?1 order by occurredAt desc",
                accountNumber);
    }

    public long countRecentTransfers(String sourceAccountNumber, LocalDateTime from) {
        return count("sourceAccount.number = ?1 and type = ?2 and occurredAt >= ?3",
                sourceAccountNumber, TransactionType.TRANSFER, from);
    }

    public long countTransfersInRange(String sourceAccountNumber, LocalDateTime from, LocalDateTime to) {
        return count("sourceAccount.number = ?1 and type = ?2 and occurredAt >= ?3 and occurredAt <= ?4",
                sourceAccountNumber, TransactionType.TRANSFER, from, to);
    }
}
