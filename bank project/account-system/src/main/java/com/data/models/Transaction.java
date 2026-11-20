package com.data.models;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    private String accountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private Currency currency = Currency.NGN;
    private TransactionStatus status = TransactionStatus.SUCCESSFUL;
    private TransactionType transactionType;
    private String transactionDescription;
    private String reference;
    @CreatedDate
    private LocalDateTime date = LocalDateTime.now();


}
