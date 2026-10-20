package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.response.AlertResponse;
import org.acme.entity.Alert;
import org.acme.repository.AlertRepository;

import java.util.List;

@ApplicationScoped
public class AlertService {

    @Inject
    AlertRepository alertRepository;

    @Transactional
    public List<AlertResponse> listAll() {
        return alertRepository.list("order by createdAt desc").stream()
                .map(this::toResponse)
                .toList();
    }

    private AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.id,
                alert.transactionId,
                alert.sourceAccountNumber,
                alert.destinationAccountNumber,
                alert.amount,
                alert.currency,
                alert.amountInUsd,
                alert.reason,
                alert.createdAt,
                alert.reviewed
        );
    }
}
