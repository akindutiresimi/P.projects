package com.data.models;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "transfers")
public class Transfers {
    @Id
    private String id;
    private String accountNumber;
    private String destinationAccountNumber;
    private String destinationBankCode;
    private String swiftBic;
    private TransactionType transactionType = TransactionType.TRANSFER_OUT;
    private String transactionDescription;
    private BigDecimal amount;
    private TransactionStatus transactionStatus = TransactionStatus.SUCCESSFUL;
    private String reference;
    @CreatedDate
    private LocalDateTime transactionDate = LocalDateTime.now();

}
