package org.acme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    public Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_account_id", nullable = false)
    public Account destinationAccount;

    @Column(nullable = false, precision = 19, scale = 4)
    public BigDecimal amount;

    @Column(name = "destination_amount", precision = 19, scale = 4)
    public BigDecimal destinationAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_currency", nullable = false, length = 3)
    public Currency sourceCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "destination_currency", nullable = false, length = 3)
    public Currency destinationCurrency;

    @Column(name = "exchange_rate", precision = 19, scale = 8)
    public BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    public TransactionType type;

    @Column(name = "occurred_at", nullable = false)
    public LocalDateTime occurredAt = LocalDateTime.now();

    @Column(length = 250)
    public String description;
}
