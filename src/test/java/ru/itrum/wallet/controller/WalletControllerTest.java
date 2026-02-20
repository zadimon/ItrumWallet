package ru.itrum.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.itrum.wallet.controller.exception.InsufficientBalanceException;
import ru.itrum.wallet.controller.exception.NotFoundException;
import ru.itrum.wallet.domain.OperationType;
import ru.itrum.wallet.domain.WalletOperation;
import ru.itrum.wallet.service.WalletService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
public class WalletControllerTest {
    private static final String BASE_URL = "/api/v1";
    private static final String PERFORM_URL = BASE_URL + "/wallet";
    private static final String GET_BALANCE = BASE_URL + "/wallets/{walletId}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @Autowired
    private ObjectMapper objectMapper;

    private final WalletOperation walletOperation = new WalletOperation(
            UUID.randomUUID(), OperationType.WITHDRAW, new BigDecimal(500)
    );

    @Test
    @DisplayName("Успешная операция")
    void performOperation_Success() throws Exception {
        doNothing().when(walletService).performOperation(eq(walletOperation));

        mockMvc.perform(post(PERFORM_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(walletOperation)))
                .andExpect(status().isOk());

        verify(walletService).performOperation(eq(walletOperation));
    }

    @Test
    @DisplayName("Недостаточно средств на кошельке")
    void performOperation_InsufficientBalance() throws Exception {
        String errorMessage = "Недостаточно средств для списания на кошельке с walletId: " + walletOperation.walletId();
        doThrow(new InsufficientBalanceException(errorMessage))
                .when(walletService)
                .performOperation(eq(walletOperation));

        mockMvc.perform(post(PERFORM_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(walletOperation)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));

        verify(walletService).performOperation(eq(walletOperation));
    }

    @Test
    @DisplayName("Операция для несуществующего кошелька")
    void performOperation_NotFound() throws Exception {
        String errorMessage = "Не найден кошелек с walletId: " + walletOperation.walletId();
        doThrow(new NotFoundException(errorMessage))
                .when(walletService)
                .performOperation(eq(walletOperation));

        mockMvc.perform(post(PERFORM_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(walletOperation)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));

        verify(walletService).performOperation(eq(walletOperation));
    }

    @Test
    @DisplayName("Невалидный запрос")
    void performOperation_InvalidJson() throws Exception {
        String jsonString = "{\"key\": \"value\"}";

        mockMvc.perform(post(PERFORM_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(status().isInternalServerError());

        verify(walletService, never()).performOperation(any());
    }

    @Test
    @DisplayName("Успешный запрос баланса")
    void getBalance_Success() throws Exception {
        BigDecimal expectedBalance = new BigDecimal("1000.00");
        when(walletService.getBalance(walletOperation.walletId()))
                .thenReturn(expectedBalance);

        mockMvc.perform(get(GET_BALANCE, walletOperation.walletId()))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.00"));

        verify(walletService).getBalance(walletOperation.walletId());
    }

    @Test
    @DisplayName("Запрос баланса для несуществующего кошелька")
    void getBalance_NotFound() throws Exception {
        String errorMessage = "Не найден кошелек с walletId: " + walletOperation.walletId();
        when(walletService.getBalance(walletOperation.walletId()))
                .thenThrow(new NotFoundException(errorMessage));

        mockMvc.perform(get(GET_BALANCE, walletOperation.walletId()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));

        verify(walletService).getBalance(walletOperation.walletId());
    }
}
