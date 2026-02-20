package ru.itrum.wallet.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletOperation(
        @NotNull(message = "Требуется id кошелька")
        UUID walletId,

        @NotNull(message = "Требуется тип операции")
        OperationType operationType,

        @NotNull(message = "Требуется сумма операции")
        @Positive(message = "Сумма должно быть положительным числом")
        BigDecimal amount
) {
}
