package com.controllers;

import com.data.models.AccountStatus;
import com.data.models.AccountType;
import com.dtos.requests.*;
import com.dtos.responses.*;
import com.services.AccountServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountServices accountServices;

    @InjectMocks
    private AccountController accountController;

    @Test
    void testCreateAccount_EndpointReturnsCreated() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId("CUST-1001");
        request.setInitialDeposit(new BigDecimal("5000.00"));

        CreateAccountResponse response = new CreateAccountResponse();
        response.setAccountNumber("1234567890");
        response.setMessage("Account created successfully");

        when(accountServices.createAccount(any(CreateAccountRequest.class))).thenReturn(response);

        ResponseEntity<?> result = accountController.createAccount(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void testDeposit_EndpointReturnsOk() {
        DepositRequest request = new DepositRequest();
        request.setAccountNumber("1234567890");
        request.setAmount(new BigDecimal("2000.00"));

        DepositResponse response = new DepositResponse();
        response.setAccountNumber("1234567890");
        response.setNewBalance(new BigDecimal("12000.00"));

        when(accountServices.deposit(any(DepositRequest.class))).thenReturn(response);

        ResponseEntity<?> result = accountController.deposit(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void testWithdraw_EndpointReturnsOk() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAccountNumber("1234567890");
        request.setAmount(new BigDecimal("1000.00"));

        WithdrawResponse response = new WithdrawResponse();
        response.setAccountNumber("1234567890");
        response.setNewBalance(new BigDecimal("9000.00"));

        when(accountServices.withdraw(any(WithdrawRequest.class))).thenReturn(response);

        ResponseEntity<?> result = accountController.withdraw(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void testTransfer_EndpointReturnsOk() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber("1234567890");
        request.setDestinationAccountNumber("0987654321");
        request.setAmount(new BigDecimal("500.00"));

        TransferResponse response = new TransferResponse();
        response.setStatus("SUCCESSFUL");

        when(accountServices.transfer(any(TransferRequest.class))).thenReturn(response);

        ResponseEntity<?> result = accountController.transfer(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void testInterbankTransfer_EndpointReturnsOk() {
        InterbankTransferRequest request = new InterbankTransferRequest();
        request.setSourceAccountNumber("1234567890");
        request.setDestinationAccountNumber("0123456789");
        request.setDestinationBankCode("058");
        request.setAmount(new BigDecimal("500.00"));

        InterbankTransferResponse response = new InterbankTransferResponse();
        response.setStatus("SUCCESSFUL");

        when(accountServices.interbankTransfer(any(InterbankTransferRequest.class))).thenReturn(response);

        ResponseEntity<?> result = accountController.interbankTransfer(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void testViewBalance_EndpointReturnsOk() {
        ViewBalanceResponse response = new ViewBalanceResponse();
        response.setAccountNumber("1234567890");
        response.setBalance(new BigDecimal("10000.00"));
        response.setAccountType(AccountType.SAVING);
        response.setStatus(AccountStatus.ACTIVE);

        when(accountServices.viewBalance("1234567890")).thenReturn(response);

        ResponseEntity<?> result = accountController.viewBalance("1234567890");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void testViewHistory_EndpointReturnsOk() {
        AccountHistoryResponse response = new AccountHistoryResponse();
        response.setAccountNumber("1234567890");
        response.setTotalTransactions(5);

        when(accountServices.viewHistory("1234567890")).thenReturn(response);

        ResponseEntity<?> result = accountController.viewHistory("1234567890");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }
}
