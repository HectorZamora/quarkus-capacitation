package org.acme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts", uniqueConstraints =
        @UniqueConstraint(name = "uk_alerts_transaction_id", columnNames = "transaction_id"))
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "transaction_id", nullable = false)
    public Long transactionId;

    @Column(name = "source_account_number", nullable = false, length = 24)
    public String sourceAccountNumber;

    @Column(name = "destination_account_number", nullable = false, length = 24)
    public String destinationAccountNumber;

    @Column(nullable = false, precision = 19, scale = 4)
    public BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    public Currency currency;

    @Column(name = "amount_in_usd", precision = 19, scale = 4)
    public BigDecimal amountInUsd;

    @Column(nullable = false, length = 500)
    public String reason;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    public boolean reviewed = false;
}
