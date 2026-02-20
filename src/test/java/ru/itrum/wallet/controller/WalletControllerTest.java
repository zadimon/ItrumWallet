package ru.itrum.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.itrum.wallet.domain.OperationType;
import ru.itrum.wallet.domain.Wallet;
import ru.itrum.wallet.domain.WalletOperation;
import ru.itrum.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WalletControllerTest {
    private static final String BASE_URL = "/api/v1";
    private static final String PERFORM_URL = BASE_URL + "/wallet";
    private static final String GET_BALANCE = BASE_URL + "/wallets/{walletId}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID existingWalletId;
    private UUID nonExistingWalletId;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();

        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal(1000));
        walletRepository.save(wallet);
        existingWalletId = wallet.getWalletId();

        nonExistingWalletId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Успешное пополнение кошелька")
    void performOperation_Deposit_Success() throws Exception {
        WalletOperation walletOperation = new WalletOperation(
                existingWalletId, OperationType.DEPOSIT, new BigDecimal(500)
        );

        mockMvc.perform(post(PERFORM_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(walletOperation)))
                .andExpect(status().isOk());

        Optional<Wallet> optionalWallet = walletRepository.findById(existingWalletId);
        Wallet updatedWallet = null;
        if (optionalWallet.isPresent()) {
            updatedWallet = optionalWallet.get();
        }
        assertNotNull(updatedWallet);
        assertEquals(new BigDecimal("1500.00"), updatedWallet.getBalance());
    }

    @Test
    @DisplayName("Успешное списание средств с кошелька")
    void performOperation_Withdraw_Success() throws Exception {
        WalletOperation walletOperation = new WalletOperation(
                existingWalletId, OperationType.WITHDRAW, new BigDecimal(500)
        );

        mockMvc.perform(post(PERFORM_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(walletOperation)))
                .andExpect(status().isOk());

        Optional<Wallet> optionalWallet = walletRepository.findById(existingWalletId);
        Wallet updatedWallet = null;
        if (optionalWallet.isPresent()) {
            updatedWallet = optionalWallet.get();
        }
        assertNotNull(updatedWallet);
        assertEquals(new BigDecimal("500.00"), updatedWallet.getBalance());
    }

    @Test
    @DisplayName("Недостаточно средств на кошельке")
    void performOperation_Withdraw_InsufficientBalance() throws Exception {
        WalletOperation walletOperation = new WalletOperation(
                existingWalletId, OperationType.WITHDRAW, new BigDecimal(1500)
        );

        mockMvc.perform(post(PERFORM_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(walletOperation)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "Недостаточно средств для списания на кошельке с walletId: " + existingWalletId
                ));
    }

    @Test
    @DisplayName("Операция для несуществующего кошелька")
    void performOperation_NotFound() throws Exception {
        WalletOperation walletOperation = new WalletOperation(
                nonExistingWalletId, OperationType.WITHDRAW, new BigDecimal(500)
        );

        mockMvc.perform(post(PERFORM_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(walletOperation)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(
                        "Не найден кошелек с walletId: " + nonExistingWalletId
                ));
    }

    @Test
    @DisplayName("Невалидный запрос")
    void performOperation_InvalidJson() throws Exception {
        String jsonString = "{\"key\": \"value\"}";

        mockMvc.perform(post(PERFORM_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Успешный запрос баланса")
    void getBalance_Success() throws Exception {
        mockMvc.perform(get(GET_BALANCE, existingWalletId))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.00"));
    }

    @Test
    @DisplayName("Запрос баланса для несуществующего кошелька")
    void getBalance_NotFound() throws Exception {
        mockMvc.perform(get(GET_BALANCE, nonExistingWalletId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(
                        "Не найден кошелек с walletId: " + nonExistingWalletId
                ));
    }
}
