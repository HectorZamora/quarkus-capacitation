package org.acme.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TransactionTypeConverter implements AttributeConverter<TransactionType, String> {

    @Override
    public String convertToDatabaseColumn(TransactionType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case DEPOSIT -> "DEPOSITO";
            case TRANSFER -> "TRANSFERENCIA";
        };
    }

    @Override
    public TransactionType convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "DEPOSITO" -> TransactionType.DEPOSIT;
            case "TRANSFERENCIA" -> TransactionType.TRANSFER;
            default -> throw new IllegalArgumentException("Unsupported persisted transaction type: " + value);
        };
    }
}
