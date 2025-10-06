package com.frisk.cadettsplitterspadel.repositories;

import com.frisk.cadettsplitterspadel.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
