package com.services;

import com.data.models.Customer;
import com.dtos.requests.CreateCustomerRequest;
import com.dtos.requests.LoginRequest;
import com.dtos.responses.CreateCustomerResponses;
import com.dtos.responses.LoginResponse;

import java.util.List;

public interface CustomerServices {
    CreateCustomerResponses sendCustomer(CreateCustomerRequest createCustomerRequest);
    CreateCustomerResponses registerCustomer(CreateCustomerRequest request);
    LoginResponse login(LoginRequest request);
    Customer findCustomerById(String customerId);
    List<Customer> findAllCustomers();
    long count();
    void deleteAll();
}
