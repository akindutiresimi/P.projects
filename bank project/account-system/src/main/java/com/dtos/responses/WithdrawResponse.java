package com.dtos.responses;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WithdrawResponse {
    private String transactionId;
    private String accountNumber;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();


}
