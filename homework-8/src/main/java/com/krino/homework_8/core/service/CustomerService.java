package com.krino.homework_8.core.service;

import com.krino.homework_8.core.model.Customer;
import com.krino.homework_8.core.model.User;
import com.krino.homework_8.core.model.UserRole;
import com.krino.homework_8.core.model.value.Address;
import com.krino.homework_8.core.repository.CustomerRepository;
import com.krino.homework_8.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    @Transactional
    public Customer createCustomer(String name, Address address) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setAddress(address);
        return customerRepository.save(customer);
    }

    public boolean isUserOfCustomer(String username, Long customerId) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            log.info("username of {} does not point to a user", username );
            return false;

        }
        User userreal = user.get();
        log.info("username : {}, customerId : {}, user_customerid : {}", username, customerId, userreal.getCustomer().getId() );
        return userreal.getCustomer().getId().equals(customerId);
    }
}