package com.krino.homework_8.core.config;

import com.krino.homework_8.core.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {
    private final CustomerService customerService;

    public boolean canAccessCustomer(Authentication auth, Long customerId) {
        String username = ((DefaultOidcUser) auth.getPrincipal()).getPreferredUsername();
        return customerService.isUserOfCustomer(username, customerId);
    }
}