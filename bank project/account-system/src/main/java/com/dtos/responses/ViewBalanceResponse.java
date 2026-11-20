package com.dtos.responses;

import com.data.models.AccountStatus;
import com.data.models.AccountType;
import com.data.models.Currency;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ViewBalanceResponse {
    private String accountNumber;
    private String customerId;
    private AccountType accountType;
    private Currency currency;
    private BigDecimal balance;
    private AccountStatus status;

}
