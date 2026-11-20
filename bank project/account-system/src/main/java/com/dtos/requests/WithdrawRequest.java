package com.dtos.requests;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class WithdrawRequest {
    private String accountNumber;
    private BigDecimal amount;
    private String pin;
    private String description = "Withdrawal";


}
