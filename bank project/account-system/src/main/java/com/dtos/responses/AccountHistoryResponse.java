package com.dtos.responses;

import com.data.models.Transaction;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class AccountHistoryResponse {
    private String accountNumber;
    private BigDecimal currentBalance;
    private int totalTransactions;
    private List<Transaction> transactions = new ArrayList<>();


}
