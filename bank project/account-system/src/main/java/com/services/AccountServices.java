package com.services;

import com.dtos.requests.*;
import com.dtos.responses.*;

public interface AccountServices {
    CreateAccountResponse sendAccount(CreateAccountRequest createAccountRequest);
    CreateAccountResponse createAccount(CreateAccountRequest request);
    DepositResponse deposit(DepositRequest request);
    WithdrawResponse withdraw(WithdrawRequest request);
    TransferResponse transfer(TransferRequest request);
    InterbankTransferResponse interbankTransfer(InterbankTransferRequest request);
    ViewBalanceResponse viewBalance(String accountNumber);
    AccountHistoryResponse viewHistory(String accountNumber);
    String generateAccountNumber();
    long count();
    void deleteAll();
}
