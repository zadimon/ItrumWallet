package ru.itrum.wallet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itrum.wallet.controller.exception.InsufficientBalanceException;
import ru.itrum.wallet.controller.exception.NotFoundException;
import ru.itrum.wallet.domain.OperationType;
import ru.itrum.wallet.domain.Wallet;
import ru.itrum.wallet.domain.WalletOperation;
import ru.itrum.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.REPEATABLE_READ;

@Service
public class WalletService {

    @Autowired
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional(isolation = REPEATABLE_READ)
    public void performOperation(WalletOperation walletOperation) {
        UUID walletId = walletOperation.walletId();
        BigDecimal amount = walletOperation.amount();

        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new NotFoundException("Не найден кошелек с walletId: " + walletId));

        BigDecimal currentBalance = wallet.getBalance();
        if (walletOperation.operationType() == OperationType.WITHDRAW) {
            if (currentBalance.compareTo(amount) < 0) {
                throw new InsufficientBalanceException(
                        "Недостаточно средств для списания на кошельке с walletId: " + walletId
                );
            }
            wallet.setBalance(currentBalance.subtract(amount));
        } else {
            wallet.setBalance(currentBalance.add(amount));
        }

        walletRepository.save(wallet);
    }

    public BigDecimal getBalance(UUID walletId) {
        return walletRepository.findById(walletId)
                .map(Wallet::getBalance)
                .orElseThrow(() -> new NotFoundException("Не найден кошелек с walletId: " + walletId));
    }
}
