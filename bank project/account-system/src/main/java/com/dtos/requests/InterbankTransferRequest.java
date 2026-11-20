package com.dtos.requests;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InterbankTransferRequest {
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private String destinationBankCode;
    private String swiftBic;
    private BigDecimal amount;
    private String pin;
    private String description = "Interbank Transfer";

}
