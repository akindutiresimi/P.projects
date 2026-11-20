package com.dtos.responses;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateCustomerResponses {
    private String id;
    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String message;
    private LocalDateTime dateCreated = LocalDateTime.now();

}
