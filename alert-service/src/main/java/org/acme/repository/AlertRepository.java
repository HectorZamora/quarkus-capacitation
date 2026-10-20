package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Alert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AlertRepository implements PanacheRepository<Alert> {

    public Optional<Alert> findByTransactionId(Long transactionId) {
        return find("transactionId", transactionId).firstResultOptional();
    }

    public List<Alert> listByDateRange(LocalDateTime from, LocalDateTime to) {
        return list("createdAt >= ?1 and createdAt <= ?2 order by createdAt desc", from, to);
    }

    public List<Alert> listBySourceAccount(String accountNumber) {
        return list("sourceAccountNumber = ?1 order by createdAt desc", accountNumber);
    }
}
