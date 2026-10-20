package org.acme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Copia local de cada evento de transferencia consumido desde Kafka.
 *
 * <p>El servicio de alertas es dueño de sus propios datos, por lo que no consulta
 * la base de datos del servicio bancario. Esta tabla es la que permite evaluar la
 * regla de "5 o más transferencias desde la misma cuenta en 5 minutos".</p>
 */
@Entity
@Table(name = "processed_transfers", uniqueConstraints =
        @UniqueConstraint(name = "uk_processed_transfers_transaction_id", columnNames = "transaction_id"))
public class ProcessedTransfer {

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

    @Column(name = "occurred_at", nullable = false)
    public LocalDateTime occurredAt;
}
