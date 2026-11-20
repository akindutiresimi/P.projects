package com.services;

import com.data.models.Customer;
import com.data.repositories.Customers;
import com.dtos.requests.CreateCustomerRequest;
import com.dtos.requests.LoginRequest;
import com.dtos.responses.CreateCustomerResponses;
import com.dtos.responses.LoginResponse;
import com.exceptions.CustomerAlreadyExistsException;
import com.exceptions.CustomerNotFoundException;
import com.exceptions.InvalidPinException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private Customers customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleCustomer = new Customer();
        sampleCustomer.setId("cust-1");
        sampleCustomer.setCustomerId("CUST-9999");
        sampleCustomer.setFirstName("Simi");
        sampleCustomer.setLastName("Akindutire");
        sampleCustomer.setEmail("simi@semicolon.africa");
        sampleCustomer.setPassword("securePassword123");
        sampleCustomer.setPhoneNumber("08012345678");
    }

    @Test
    public void testRegisterCustomer_Success() {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setFirstName("Simi");
        request.setLastName("Akindutire");
        request.setEmail("simi@semicolon.africa");
        request.setPhoneNumber("08012345678");
        request.setPassword("securePassword123");

        when(customerRepository.existsByEmail("simi@semicolon.africa")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            c.setId("cust-1");
            return c;
        });

        CreateCustomerResponses response = customerService.registerCustomer(request);

        assertNotNull(response);
        assertEquals("Simi", response.getFirstName());
        assertEquals("Akindutire", response.getLastName());
        assertEquals("simi@semicolon.africa", response.getEmail());
        assertNotNull(response.getCustomerId());
        assertTrue(response.getCustomerId().startsWith("CUST-"));
        assertEquals("Customer registered successfully", response.getMessage());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    public void testRegisterCustomer_DuplicateEmailThrowsException() {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setEmail("simi@semicolon.africa");

        when(customerRepository.existsByEmail("simi@semicolon.africa")).thenReturn(true);

        assertThrows(CustomerAlreadyExistsException.class, () -> customerService.registerCustomer(request));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    public void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("simi@semicolon.africa");
        request.setPassword("securePassword123");

        when(customerRepository.findByEmail("simi@semicolon.africa")).thenReturn(Optional.of(sampleCustomer));

        LoginResponse response = customerService.login(request);

        assertNotNull(response);
        assertEquals("CUST-9999", response.getCustomerId());
        assertEquals("Simi Akindutire", response.getFullName());
        assertNotNull(response.getToken());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    public void testLogin_InvalidPasswordThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("simi@semicolon.africa");
        request.setPassword("wrongPassword");

        when(customerRepository.findByEmail("simi@semicolon.africa")).thenReturn(Optional.of(sampleCustomer));

        assertThrows(InvalidPinException.class, () -> customerService.login(request));
    }

    @Test
    public void testLogin_CustomerNotFoundThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@semicolon.africa");
        request.setPassword("password");

        when(customerRepository.findByEmail("unknown@semicolon.africa")).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.login(request));
    }

    @Test
    public void testFindCustomerById_Success() {
        when(customerRepository.findByCustomerId("CUST-9999")).thenReturn(Optional.of(sampleCustomer));

        Customer found = customerService.findCustomerById("CUST-9999");

        assertNotNull(found);
        assertEquals("CUST-9999", found.getCustomerId());
        assertEquals("Simi", found.getFirstName());
    }
}