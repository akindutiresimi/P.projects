package com.services;

import com.data.models.*;
import com.data.repositories.Accounts;
import com.data.repositories.Customers;
import com.data.repositories.Transactions;
import com.data.repositories.TransfersRepository;
import com.dtos.requests.*;
import com.dtos.responses.*;
import com.exceptions.InsufficientFundsException;
import com.exceptions.InvalidAmountException;
import com.exceptions.InvalidPinException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private Accounts accountRepository;

    @Mock
    private Customers customerRepository;

    @Mock
    private Transactions transactionRepository;

    @Mock
    private TransfersRepository transfersRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Customer sampleCustomer;
    private Account sampleAccount;

    @BeforeEach
    void setUp() {
        sampleCustomer = new Customer();
        sampleCustomer.setId("cust-id-1");
        sampleCustomer.setCustomerId("CUST-1001");
        sampleCustomer.setFirstName("Simi");
        sampleCustomer.setLastName("Akindutire");
        sampleCustomer.setEmail("simi@semicolon.africa");

        sampleAccount = new Account();
        sampleAccount.setId("acc-id-1");
        sampleAccount.setCustomerId("CUST-1001");
        sampleAccount.setAccountNumber("1234567890");
        sampleAccount.setBalance(new BigDecimal("10000.00"));
        sampleAccount.setPin("1234");
        sampleAccount.setAccountStatus(AccountStatus.ACTIVE);
    }

    @Test
    public void createAccountForACustomer_AnTheItReturnOneAccount() {
        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setName("Simi Akindutire");
        createAccountRequest.setCustomerId("CUST-1001");
        createAccountRequest.setInitialDeposit(new BigDecimal("5000.00"));
        createAccountRequest.setPin("1234");

        when(customerRepository.findByCustomerId("CUST-1001")).thenReturn(Optional.of(sampleCustomer));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            acc.setId("acc-id-new");
            return acc;
        });

        CreateAccountResponse response = accountService.createAccount(createAccountRequest);

        assertNotNull(response);
        assertNotNull(response.getAccountNumber());
        assertEquals("CUST-1001", response.getCustomerId());
        assertEquals(new BigDecimal("5000.00"), response.getBalance());
        assertEquals("Account created successfully", response.getMessage());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    public void testDepositMoneyIntoAccount_BalanceIncreases() {
        DepositRequest request = new DepositRequest();
        request.setAccountNumber("1234567890");
        request.setAmount(new BigDecimal("2500.00"));

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(sampleAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction txn = invocation.getArgument(0);
            txn.setId("txn-1");
            return txn;
        });

        DepositResponse response = accountService.deposit(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("12500.00"), response.getNewBalance());
        assertEquals("Deposit successful", response.getMessage());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testDepositNegativeAmount_ThrowsInvalidAmountException() {
        DepositRequest request = new DepositRequest();
        request.setAccountNumber("1234567890");
        request.setAmount(new BigDecimal("-500.00"));

        assertThrows(InvalidAmountException.class, () -> accountService.deposit(request));
    }

    @Test
    public void testWithdrawMoneyWithValidPinAndBalance_BalanceDecreases() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAccountNumber("1234567890");
        request.setAmount(new BigDecimal("3000.00"));
        request.setPin("1234");

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(sampleAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction txn = invocation.getArgument(0);
            txn.setId("txn-2");
            return txn;
        });

        WithdrawResponse response = accountService.withdraw(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("7000.00"), response.getNewBalance());
        assertEquals("Withdrawal successful", response.getMessage());
    }

    @Test
    public void testWithdrawWithInsufficientBalance_ThrowsInsufficientFundsException() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAccountNumber("1234567890");
        request.setAmount(new BigDecimal("50000.00"));
        request.setPin("1234");

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(sampleAccount));

        assertThrows(InsufficientFundsException.class, () -> accountService.withdraw(request));
    }

    @Test
    public void testWithdrawWithInvalidPin_ThrowsInvalidPinException() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAccountNumber("1234567890");
        request.setAmount(new BigDecimal("1000.00"));
        request.setPin("9999");

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(sampleAccount));

        assertThrows(InvalidPinException.class, () -> accountService.withdraw(request));
    }

    @Test
    public void testTransferMoneyBetweenTwoAccounts_BalancesUpdated() {
        Account destinationAccount = new Account();
        destinationAccount.setId("acc-id-2");
        destinationAccount.setAccountNumber("0987654321");
        destinationAccount.setBalance(new BigDecimal("1000.00"));
        destinationAccount.setAccountStatus(AccountStatus.ACTIVE);

        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber("1234567890");
        request.setDestinationAccountNumber("0987654321");
        request.setAmount(new BigDecimal("2000.00"));
        request.setPin("1234");

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(sampleAccount));
        when(accountRepository.findByAccountNumber("0987654321")).thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction txn = invocation.getArgument(0);
            txn.setId("txn-trf");
            return txn;
        });

        TransferResponse response = accountService.transfer(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("8000.00"), response.getRemainingBalance());
        assertEquals(new BigDecimal("3000.00"), destinationAccount.getBalance());
        assertEquals("SUCCESSFUL", response.getStatus());
        verify(transfersRepository, times(1)).save(any(Transfers.class));
    }

    @Test
    public void testInterbankTransfer_BalanceDebitedAndTransferRecorded() {
        InterbankTransferRequest request = new InterbankTransferRequest();
        request.setSourceAccountNumber("1234567890");
        request.setDestinationAccountNumber("0123456789");
        request.setDestinationBankCode("058");
        request.setSwiftBic("GTBINGLA");
        request.setAmount(new BigDecimal("4000.00"));
        request.setPin("1234");

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(sampleAccount));
        when(transfersRepository.save(any(Transfers.class))).thenAnswer(invocation -> {
            Transfers t = invocation.getArgument(0);
            t.setId("ib-1");
            return t;
        });

        InterbankTransferResponse response = accountService.interbankTransfer(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("6000.00"), response.getRemainingBalance());
        assertEquals("058", response.getDestinationBankCode());
        assertEquals("SUCCESSFUL", response.getStatus());
        verify(transfersRepository, times(1)).save(any(Transfers.class));
    }

    @Test
    public void testViewBalance_ReturnsCorrectBalance() {
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(sampleAccount));

        ViewBalanceResponse response = accountService.viewBalance("1234567890");

        assertNotNull(response);
        assertEquals("1234567890", response.getAccountNumber());
        assertEquals(new BigDecimal("10000.00"), response.getBalance());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
    }

    @Test
    public void testViewHistory_ReturnsTransactionsInOrder() {
        Transaction t1 = new Transaction();
        t1.setId("t1");
        t1.setAccountNumber("1234567890");
        t1.setAmount(new BigDecimal("5000.00"));
        t1.setDate(LocalDateTime.now().minusDays(1));

        Transaction t2 = new Transaction();
        t2.setId("t2");
        t2.setAccountNumber("1234567890");
        t2.setAmount(new BigDecimal("2000.00"));
        t2.setDate(LocalDateTime.now());

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(sampleAccount));
        when(transactionRepository.findByAccountNumberOrderByDateDesc("1234567890")).thenReturn(List.of(t2, t1));

        AccountHistoryResponse response = accountService.viewHistory("1234567890");

        assertNotNull(response);
        assertEquals(2, response.getTotalTransactions());
        assertEquals(2, response.getTransactions().size());
    }
}