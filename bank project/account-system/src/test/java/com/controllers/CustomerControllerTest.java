package com.controllers;

import com.dtos.requests.CreateCustomerRequest;
import com.dtos.requests.LoginRequest;
import com.dtos.responses.CreateCustomerResponses;
import com.dtos.responses.LoginResponse;
import com.services.CustomerServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerServices customerServices;

    @InjectMocks
    private CustomerController customerController;

    private CreateCustomerRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new CreateCustomerRequest();
        registerRequest.setFirstName("Simi");
        registerRequest.setLastName("Akindutire");
        registerRequest.setEmail("simi@semicolon.africa");
    }

    @Test
    void testRegisterCustomer_EndpointReturnsCreated() {
        CreateCustomerResponses response = new CreateCustomerResponses();
        response.setCustomerId("CUST-1001");
        response.setMessage("Customer registered successfully");

        when(customerServices.registerCustomer(any(CreateCustomerRequest.class))).thenReturn(response);

        ResponseEntity<?> result = customerController.registerCustomer(registerRequest);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void testLogin_EndpointReturnsOk() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("simi@semicolon.africa");
        loginRequest.setPassword("password");

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setCustomerId("CUST-1001");
        loginResponse.setMessage("Login successful");

        when(customerServices.login(any(LoginRequest.class))).thenReturn(loginResponse);

        ResponseEntity<?> result = customerController.login(loginRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }
}
