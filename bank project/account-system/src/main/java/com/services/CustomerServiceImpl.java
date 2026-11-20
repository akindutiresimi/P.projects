package com.services;

import com.data.models.AccountStatus;
import com.data.models.Customer;
import com.data.repositories.Customers;
import com.dtos.requests.CreateCustomerRequest;
import com.dtos.requests.LoginRequest;
import com.dtos.responses.CreateCustomerResponses;
import com.dtos.responses.LoginResponse;
import com.exceptions.CustomerAlreadyExistsException;
import com.exceptions.CustomerNotFoundException;
import com.exceptions.InvalidPinException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerServiceImpl implements CustomerServices {

    private final Customers customerRepository;

    @Autowired
    public CustomerServiceImpl(Customers customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CreateCustomerResponses sendCustomer(CreateCustomerRequest createCustomerRequest) {
        return registerCustomer(createCustomerRequest);
    }

    @Override
    public CreateCustomerResponses registerCustomer(CreateCustomerRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Customer request cannot be null");
        }
        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail())) {
            throw new CustomerAlreadyExistsException("Customer with email " + request.getEmail() + " already exists");
        }

        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setPassword(request.getPassword());
        customer.setBvn(request.getBvn());
        customer.setAddress(request.getAddress());
        customer.setStatus(AccountStatus.ACTIVE);

        String generatedCustomerId = "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        customer.setCustomerId(generatedCustomerId);

        Customer savedCustomer = customerRepository.save(customer);

        CreateCustomerResponses response = new CreateCustomerResponses();
        response.setId(savedCustomer.getId());
        response.setCustomerId(savedCustomer.getCustomerId());
        response.setFirstName(savedCustomer.getFirstName());
        response.setLastName(savedCustomer.getLastName());
        response.setEmail(savedCustomer.getEmail());
        response.setPhoneNumber(savedCustomer.getPhoneNumber());
        response.setMessage("Customer registered successfully");
        response.setDateCreated(LocalDateTime.now());
        return response;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        if (request == null || request.getEmail() == null) {
            throw new IllegalArgumentException("Invalid login credentials");
        }
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomerNotFoundException("Invalid email or password"));

        if (customer.getPassword() != null && !customer.getPassword().equals(request.getPassword())) {
            throw new InvalidPinException("Invalid email or password");
        }

        LoginResponse response = new LoginResponse();
        response.setCustomerId(customer.getCustomerId());
        response.setFullName(customer.getFullName());
        response.setEmail(customer.getEmail());
        response.setToken("JWT-" + UUID.randomUUID().toString());
        response.setMessage("Login successful");
        return response;
    }

    @Override
    public Customer findCustomerById(String customerId) {
        return customerRepository.findByCustomerId(customerId)
                .or(() -> customerRepository.findById(customerId))
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));
    }

    @Override
    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public long count() {
        return customerRepository.count();
    }

    @Override
    public void deleteAll() {
        customerRepository.deleteAll();
    }
}
