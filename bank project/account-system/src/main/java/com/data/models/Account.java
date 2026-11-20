package com.data.models;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "accounts")
public class Account {
    @Id
    private String id;
    private String customerId;
    private String accountNumber;
    private AccountType accountType = AccountType.SAVING;
    private AccountStatus accountStatus = AccountStatus.ACTIVE;
    private Currency currency = Currency.NGN;
    private BigDecimal balance = BigDecimal.ZERO;
    private String pin;
    @CreatedDate
    private LocalDateTime createdAt = LocalDateTime.now();
    @LastModifiedDate
    private LocalDateTime updatedAt = LocalDateTime.now();


    public void credit(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            if (this.balance == null) {
                this.balance = BigDecimal.ZERO;
            }
            this.balance = this.balance.add(amount);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void debit(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            if (this.balance == null) {
                this.balance = BigDecimal.ZERO;
            }
            if (this.balance.compareTo(amount) >= 0) {
                this.balance = this.balance.subtract(amount);
                this.updatedAt = LocalDateTime.now();
            }
        }
    }
}
