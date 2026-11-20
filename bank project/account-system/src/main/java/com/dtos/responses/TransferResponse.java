package com.dtos.responses;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class TransferResponse {
    private String transactionId;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private BigDecimal remainingBalance;
    private String reference;
    private String status;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();

//
}
