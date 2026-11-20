package com.dtos.requests;

import com.data.models.AccountType;
import com.data.models.Currency;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountRequest {
    private String customerId;
    private String name;
    private AccountType accountType = AccountType.SAVING;
    private Currency currency = Currency.NGN;
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal initialDeposit = BigDecimal.ZERO;
    private String pin = "1234";

}
