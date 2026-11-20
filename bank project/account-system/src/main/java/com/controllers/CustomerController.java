package com.controllers;

import com.dtos.requests.CreateCustomerRequest;
import com.dtos.requests.LoginRequest;
import com.dtos.responses.ApiResponse;
import com.dtos.responses.CreateCustomerResponses;
import com.dtos.responses.LoginResponse;
import com.services.CustomerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerServices customerServices;

    @Autowired
    public CustomerController(CustomerServices customerServices) {
        this.customerServices = customerServices;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerCustomer(@RequestBody CreateCustomerRequest request) {
        try {
            CreateCustomerResponses response = customerServices.registerCustomer(request);
            return new ResponseEntity<>(new ApiResponse(true, response), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = customerServices.login(request);
            return new ResponseEntity<>(new ApiResponse(true, response), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse> getCustomer(@PathVariable String customerId) {
        try {
            return new ResponseEntity<>(new ApiResponse(true, customerServices.findCustomerById(customerId)), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
}
