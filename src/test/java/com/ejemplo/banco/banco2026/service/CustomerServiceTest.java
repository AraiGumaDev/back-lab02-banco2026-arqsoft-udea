package com.ejemplo.banco.banco2026.service;

import com.ejemplo.banco.banco2026.DTO.CustomerDTO;
import com.ejemplo.banco.banco2026.entity.Customer;
import com.ejemplo.banco.banco2026.exception.ResourceNotFoundException;
import com.ejemplo.banco.banco2026.mapper.CustomerMapper;
import com.ejemplo.banco.banco2026.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        // Se usa el mapper real generado por MapStruct
        CustomerMapper customerMapper = Mappers.getMapper(CustomerMapper.class);
        customerService = new CustomerService(customerRepository, customerMapper);
    }

    private Customer customer(Long id, String account, double balance) {
        return new Customer(id, account, "Ana", "Lopez", balance);
    }

    @Test
    void getAllCustomerReturnsMappedList() {
        when(customerRepository.findAll()).thenReturn(List.of(
                customer(1L, "111", 100.0),
                customer(2L, "222", 200.0)));

        List<CustomerDTO> result = customerService.getAllCustomer();

        assertEquals(2, result.size());
        assertEquals("111", result.get(0).getAccountNumber());
        assertEquals(200.0, result.get(1).getBalance());
    }

    @Test
    void getCustomerByIdReturnsCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer(1L, "111", 100.0)));

        CustomerDTO result = customerService.getCustomerById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Ana", result.getFirstName());
    }

    @Test
    void getCustomerByIdThrowsWhenNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> customerService.getCustomerById(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void createCustomerIgnoresIncomingId() {
        CustomerDTO input = new CustomerDTO(50L, "Ana", "Lopez", "111", 100.0);
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CustomerDTO result = customerService.createCustomer(input);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertEquals(1L, result.getId());
        assertEquals("111", captor.getValue().getAccountNumber());
    }

    @Test
    void updateCustomerOnlyChangesProvidedFields() {
        Customer existing = customer(1L, "111", 100.0);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerDTO changes = new CustomerDTO();
        changes.setFirstName("Maria");
        changes.setBalance(500.0);

        CustomerDTO result = customerService.updateCustomer(1L, changes);

        assertEquals("Maria", result.getFirstName());
        assertEquals("Lopez", result.getLastName());
        assertEquals("111", result.getAccountNumber());
        assertEquals(500.0, result.getBalance());
    }

    @Test
    void updateCustomerChangesAllFields() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer(1L, "111", 100.0)));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerDTO result = customerService.updateCustomer(1L,
                new CustomerDTO(null, "Luis", "Perez", "999", 10.0));

        assertEquals("Luis", result.getFirstName());
        assertEquals("Perez", result.getLastName());
        assertEquals("999", result.getAccountNumber());
        assertEquals(10.0, result.getBalance());
    }

    @Test
    void updateCustomerThrowsWhenNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        CustomerDTO changes = new CustomerDTO();

        assertThrows(ResourceNotFoundException.class, () -> customerService.updateCustomer(1L, changes));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void deleteCustomerDeletesWhenExists() {
        when(customerRepository.existsById(1L)).thenReturn(true);

        customerService.deleteCustomer(1L);

        verify(customerRepository).deleteById(1L);
    }

    @Test
    void deleteCustomerThrowsWhenNotFound() {
        when(customerRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> customerService.deleteCustomer(1L));
        verify(customerRepository, never()).deleteById(any());
    }
}
