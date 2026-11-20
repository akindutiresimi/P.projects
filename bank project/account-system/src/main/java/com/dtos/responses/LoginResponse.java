package com.dtos.responses;

import lombok.Data;

@Data
public class LoginResponse {
    private String customerId;
    private String fullName;
    private String email;
    private String token;
    private String message;

}
