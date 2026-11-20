package com.dtos.responses;

import com.data.models.AccountType;
import com.data.models.Currency;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountResponse {
    private String id;
    private String accountId;
    private String customerId;
    private String accountNumber;
    private AccountType accountType;
    private Currency currency;
    private BigDecimal balance;
    private String message;

}
