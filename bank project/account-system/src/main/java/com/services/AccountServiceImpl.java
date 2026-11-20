package com.services;

import com.data.models.*;
import com.data.repositories.Accounts;
import com.data.repositories.Customers;
import com.data.repositories.Transactions;
import com.data.repositories.TransfersRepository;
import com.dtos.requests.*;
import com.dtos.responses.*;
import com.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountServices {

    private final Accounts accountRepository;
    private final Customers customerRepository;
    private final Transactions transactionRepository;
    private final TransfersRepository transfersRepository;
    private final Random random = new Random();

    @Autowired
    public AccountServiceImpl(Accounts accountRepository,
                              Customers customerRepository,
                              Transactions transactionRepository,
                              TransfersRepository transfersRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.transfersRepository = transfersRepository;
    }

    @Override
    public CreateAccountResponse sendAccount(CreateAccountRequest createAccountRequest) {
        return createAccount(createAccountRequest);
    }

    @Override
    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CreateAccountRequest cannot be null");
        }

        String customerId = request.getCustomerId();
        if (customerId == null || customerId.trim().isEmpty()) {
            Customer newCustomer = new Customer();
            String name = request.getName() != null ? request.getName() : "Valued Customer";
            String[] parts = name.split(" ", 2);
            newCustomer.setFirstName(parts[0]);
            newCustomer.setLastName(parts.length > 1 ? parts[1] : "");
            newCustomer.setCustomerId("CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            newCustomer = customerRepository.save(newCustomer);
            customerId = newCustomer.getCustomerId();
        }

        Account account = new Account();
        account.setCustomerId(customerId);
        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(request.getAccountType() != null ? request.getAccountType() : AccountType.SAVING);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setCurrency(request.getCurrency() != null ? request.getCurrency() : Currency.NGN);
        account.setPin(request.getPin() != null ? request.getPin() : "1234");

        BigDecimal initialDeposit = request.getInitialDeposit();
        if (initialDeposit != null && initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            account.setBalance(initialDeposit);
        } else if (request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            initialDeposit = request.getAmount();
            account.setBalance(initialDeposit);
        } else {
            account.setBalance(BigDecimal.ZERO);
        }

        Account savedAccount = accountRepository.save(account);

        Customer customer = customerRepository.findByCustomerId(customerId).orElse(null);
        if (customer != null) {
            customer.getAccountNumbers().add(savedAccount.getAccountNumber());
            customerRepository.save(customer);
        }

        if (initialDeposit != null && initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            Transaction depositTxn = new Transaction();
            depositTxn.setAccountNumber(savedAccount.getAccountNumber());
            depositTxn.setAmount(initialDeposit);
            depositTxn.setCurrency(savedAccount.getCurrency());
            depositTxn.setStatus(TransactionStatus.SUCCESSFUL);
            depositTxn.setTransactionType(TransactionType.DEPOSIT);
            depositTxn.setTransactionDescription("Initial deposit");
            depositTxn.setReference("INIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            depositTxn.setDate(LocalDateTime.now());
            transactionRepository.save(depositTxn);
        }

        CreateAccountResponse response = new CreateAccountResponse();
        response.setId(savedAccount.getId());
        response.setAccountId(savedAccount.getId());
        response.setCustomerId(savedAccount.getCustomerId());
        response.setAccountNumber(savedAccount.getAccountNumber());
        response.setAccountType(savedAccount.getAccountType());
        response.setCurrency(savedAccount.getCurrency());
        response.setBalance(savedAccount.getBalance());
        response.setMessage("Account created successfully");
        return response;
    }

    @Override
    public DepositResponse deposit(DepositRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Deposit request cannot be null");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero");
        }

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + request.getAccountNumber()));

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException("Account is not active");
        }

        account.credit(request.getAmount());
        Account updatedAccount = accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccountNumber(updatedAccount.getAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(updatedAccount.getCurrency());
        transaction.setStatus(TransactionStatus.SUCCESSFUL);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setTransactionDescription(request.getDescription() != null ? request.getDescription() : "Deposit");
        transaction.setReference("DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setDate(LocalDateTime.now());
        Transaction savedTxn = transactionRepository.save(transaction);

        DepositResponse response = new DepositResponse();
        response.setTransactionId(savedTxn.getId() != null ? savedTxn.getId() : savedTxn.getReference());
        response.setAccountNumber(updatedAccount.getAccountNumber());
        response.setAmount(request.getAmount());
        response.setNewBalance(updatedAccount.getBalance());
        response.setMessage("Deposit successful");
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    @Override
    public WithdrawResponse withdraw(WithdrawRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Withdraw request cannot be null");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero");
        }

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + request.getAccountNumber()));

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException("Account is not active");
        }

        if (account.getPin() != null && request.getPin() != null && !account.getPin().equals(request.getPin())) {
            throw new InvalidPinException("Invalid PIN");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Available balance: " + account.getBalance());
        }

        account.debit(request.getAmount());
        Account updatedAccount = accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccountNumber(updatedAccount.getAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(updatedAccount.getCurrency());
        transaction.setStatus(TransactionStatus.SUCCESSFUL);
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setTransactionDescription(request.getDescription() != null ? request.getDescription() : "Withdrawal");
        transaction.setReference("WTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setDate(LocalDateTime.now());
        Transaction savedTxn = transactionRepository.save(transaction);

        WithdrawResponse response = new WithdrawResponse();
        response.setTransactionId(savedTxn.getId() != null ? savedTxn.getId() : savedTxn.getReference());
        response.setAccountNumber(updatedAccount.getAccountNumber());
        response.setAmount(request.getAmount());
        response.setNewBalance(updatedAccount.getBalance());
        response.setMessage("Withdrawal successful");
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    @Override
    public TransferResponse transfer(TransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Transfer request cannot be null");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than zero");
        }
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }

        Account sourceAccount = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Source account not found: " + request.getSourceAccountNumber()));

        Account destinationAccount = accountRepository.findByAccountNumber(request.getDestinationAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Destination account not found: " + request.getDestinationAccountNumber()));

        if (sourceAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException("Source account is not active");
        }
        if (destinationAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException("Destination account is not active");
        }

        if (sourceAccount.getPin() != null && request.getPin() != null && !sourceAccount.getPin().equals(request.getPin())) {
            throw new InvalidPinException("Invalid PIN");
        }

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Available balance: " + sourceAccount.getBalance());
        }

        sourceAccount.debit(request.getAmount());
        destinationAccount.credit(request.getAmount());

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        String reference = "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Transaction debitTxn = new Transaction();
        debitTxn.setAccountNumber(sourceAccount.getAccountNumber());
        debitTxn.setDestinationAccountNumber(destinationAccount.getAccountNumber());
        debitTxn.setAmount(request.getAmount());
        debitTxn.setCurrency(sourceAccount.getCurrency());
        debitTxn.setStatus(TransactionStatus.SUCCESSFUL);
        debitTxn.setTransactionType(TransactionType.TRANSFER_OUT);
        debitTxn.setTransactionDescription("Transfer to " + destinationAccount.getAccountNumber());
        debitTxn.setReference(reference);
        debitTxn.setDate(LocalDateTime.now());
        Transaction savedDebitTxn = transactionRepository.save(debitTxn);

        Transaction creditTxn = new Transaction();
        creditTxn.setAccountNumber(destinationAccount.getAccountNumber());
        creditTxn.setDestinationAccountNumber(sourceAccount.getAccountNumber());
        creditTxn.setAmount(request.getAmount());
        creditTxn.setCurrency(destinationAccount.getCurrency());
        creditTxn.setStatus(TransactionStatus.SUCCESSFUL);
        creditTxn.setTransactionType(TransactionType.TRANSFER_IN);
        creditTxn.setTransactionDescription("Transfer from " + sourceAccount.getAccountNumber());
        creditTxn.setReference(reference);
        creditTxn.setDate(LocalDateTime.now());
        transactionRepository.save(creditTxn);

        Transfers transferRecord = new Transfers();
        transferRecord.setAccountNumber(sourceAccount.getAccountNumber());
        transferRecord.setDestinationAccountNumber(destinationAccount.getAccountNumber());
        transferRecord.setAmount(request.getAmount());
        transferRecord.setTransactionType(TransactionType.TRANSFER_OUT);
        transferRecord.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transferRecord.setTransactionDescription(request.getDescription());
        transferRecord.setReference(reference);
        transferRecord.setTransactionDate(LocalDateTime.now());
        transfersRepository.save(transferRecord);

        TransferResponse response = new TransferResponse();
        response.setTransactionId(savedDebitTxn.getId() != null ? savedDebitTxn.getId() : reference);
        response.setSourceAccountNumber(sourceAccount.getAccountNumber());
        response.setDestinationAccountNumber(destinationAccount.getAccountNumber());
        response.setAmount(request.getAmount());
        response.setRemainingBalance(sourceAccount.getBalance());
        response.setReference(reference);
        response.setStatus("SUCCESSFUL");
        response.setMessage("Transfer completed successfully");
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    @Override
    public InterbankTransferResponse interbankTransfer(InterbankTransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Interbank transfer request cannot be null");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than zero");
        }

        Account sourceAccount = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Source account not found: " + request.getSourceAccountNumber()));

        if (sourceAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException("Source account is not active");
        }

        if (sourceAccount.getPin() != null && request.getPin() != null && !sourceAccount.getPin().equals(request.getPin())) {
            throw new InvalidPinException("Invalid PIN");
        }

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Available balance: " + sourceAccount.getBalance());
        }

        sourceAccount.debit(request.getAmount());
        accountRepository.save(sourceAccount);

        String reference = "IB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Transaction txn = new Transaction();
        txn.setAccountNumber(sourceAccount.getAccountNumber());
        txn.setDestinationAccountNumber(request.getDestinationAccountNumber());
        txn.setAmount(request.getAmount());
        txn.setCurrency(sourceAccount.getCurrency());
        txn.setStatus(TransactionStatus.SUCCESSFUL);
        txn.setTransactionType(TransactionType.INTER_BANK_TRANSFER);
        txn.setTransactionDescription("Interbank transfer to Bank: " + request.getDestinationBankCode() + ", Acc: " + request.getDestinationAccountNumber());
        txn.setReference(reference);
        txn.setDate(LocalDateTime.now());
        transactionRepository.save(txn);

        Transfers transferRecord = new Transfers();
        transferRecord.setAccountNumber(sourceAccount.getAccountNumber());
        transferRecord.setDestinationAccountNumber(request.getDestinationAccountNumber());
        transferRecord.setDestinationBankCode(request.getDestinationBankCode());
        transferRecord.setSwiftBic(request.getSwiftBic());
        transferRecord.setAmount(request.getAmount());
        transferRecord.setTransactionType(TransactionType.INTER_BANK_TRANSFER);
        transferRecord.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transferRecord.setTransactionDescription(request.getDescription());
        transferRecord.setReference(reference);
        transferRecord.setTransactionDate(LocalDateTime.now());
        Transfers savedTransfer = transfersRepository.save(transferRecord);

        InterbankTransferResponse response = new InterbankTransferResponse();
        response.setTransferId(savedTransfer.getId() != null ? savedTransfer.getId() : reference);
        response.setSourceAccountNumber(sourceAccount.getAccountNumber());
        response.setDestinationAccountNumber(request.getDestinationAccountNumber());
        response.setDestinationBankCode(request.getDestinationBankCode());
        response.setAmount(request.getAmount());
        response.setRemainingBalance(sourceAccount.getBalance());
        response.setReference(reference);
        response.setStatus("SUCCESSFUL");
        response.setMessage("Interbank transfer dispatched successfully");
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    @Override
    public ViewBalanceResponse viewBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));

        ViewBalanceResponse response = new ViewBalanceResponse();
        response.setAccountNumber(account.getAccountNumber());
        response.setCustomerId(account.getCustomerId());
        response.setAccountType(account.getAccountType());
        response.setCurrency(account.getCurrency());
        response.setBalance(account.getBalance());
        response.setStatus(account.getAccountStatus());
        return response;
    }

    @Override
    public AccountHistoryResponse viewHistory(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));

        List<Transaction> transactions = transactionRepository.findByAccountNumberOrderByDateDesc(accountNumber);

        AccountHistoryResponse response = new AccountHistoryResponse();
        response.setAccountNumber(account.getAccountNumber());
        response.setCurrentBalance(account.getBalance());
        response.setTotalTransactions(transactions.size());
        response.setTransactions(transactions);
        return response;
    }

    @Override
    public String generateAccountNumber() {
        String accountNumber;
        do {
            long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    @Override
    public long count() {
        return accountRepository.count();
    }

    @Override
    public void deleteAll() {
        accountRepository.deleteAll();
    }
}
