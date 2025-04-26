package com.krino.homework_8.core.service;

import com.krino.homework_8.core.model.Customer;
import com.krino.homework_8.core.model.User;
import com.krino.homework_8.core.model.UserRole;
import com.krino.homework_8.core.repository.CustomerRepository;
import com.krino.homework_8.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createCustomerUser(String username, String password, Customer customer) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.ROLE_CUSTOMER);
        user.setCustomer(customer);
        return userRepository.save(user);
    }

    @Transactional
    public User createAdminUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.ROLE_ADMIN);
        return userRepository.save(user);
    }

    public boolean hasAdminAccess(User user) {
        return user != null && UserRole.ROLE_ADMIN.equals(user.getRole());
    }

    public boolean canAccessCustomer(User user, Long customerId) {
        if (user == null) {
            return false;
        }

        if (UserRole.ROLE_ADMIN.equals(user.getRole())) {
            return true;
        }

        Customer customer = user.getCustomer();
        return customer != null && customer.getId().equals(customerId);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}