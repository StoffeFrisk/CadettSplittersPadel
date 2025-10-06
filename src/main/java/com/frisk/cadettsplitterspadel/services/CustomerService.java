package com.frisk.cadettsplitterspadel.services;

import com.frisk.cadettsplitterspadel.entities.Customer;
import com.frisk.cadettsplitterspadel.exceptions.ResourceNotFoundException;
import com.frisk.cadettsplitterspadel.repositories.CustomerRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Optional<Customer> findByUserId(String userId) {
        return customerRepository.findByUserId(userId);
    }

    public boolean existsByUserId(String userId) {
        return customerRepository.existsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Customer getForUserOrThrow(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        return customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "userId", userId));
    }
}
