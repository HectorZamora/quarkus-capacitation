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
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts", uniqueConstraints =
        @UniqueConstraint(name = "uk_accounts_number", columnNames = "number"))
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "number", nullable = false, length = 24)
    public String number;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    public Currency currency;

    @Column(nullable = false, precision = 19, scale = 4)
    public BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    public boolean active = true;

    @Column(name = "opened_at", nullable = false)
    public LocalDateTime openedAt = LocalDateTime.now();
}
