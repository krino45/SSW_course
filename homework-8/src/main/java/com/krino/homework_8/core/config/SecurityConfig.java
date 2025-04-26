package com.krino.homework_8.core.config;

import com.krino.homework_8.core.model.Customer;
import com.krino.homework_8.core.service.CustomerService;
import com.krino.homework_8.core.service.UserService;

import java.util.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        OidcClientInitiatedLogoutSuccessHandler logoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        logoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/");


        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/orders").hasRole("ADMIN")
                        .requestMatchers("/orders/customer/**").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/orders").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/orders/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/orders/**").hasRole("ADMIN")
                        .anyRequest().authenticated()

                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(new JwtAuthenticationConverter())
                        )
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(this.oidcUserService())
                        )
                ).logout(l->l
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                );
        return http.build();
    }

    private final UserService userService;
    private final CustomerService customerService;

    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        var delegate = new OidcUserService();
        return userRequest -> {
            // load the info from Keycloak
            OidcUser oidcUser = delegate.loadUser(userRequest);

            // 2) pull realm_access.roles
            Map<String, Object> realmAccess = oidcUser.getClaim("realm_access");
            Collection<GrantedAuthority> mappedAuthorities = new HashSet<>();
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                for (String role : roles) {
                    // turn "admin" into "ROLE_ADMIN"
                    mappedAuthorities.add(new SimpleGrantedAuthority(role.toUpperCase()));
                }
            }

            // 3) (optionally) pull client roles from resource_access.<client>.roles
            Map<String, Object> resourceAccess =
                    oidcUser.getClaim("resource_access");
            if (resourceAccess != null) {
                // replace "spring-api" with your client-id if needed
                if (resourceAccess.containsKey("spring-api")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> client = (Map<String, Object>) ((Map<String, Object>) resourceAccess).get("spring-api");
                    @SuppressWarnings("unchecked")
                    List<String> clientRoles = (List<String>) client.get("roles");
                    for (String role : clientRoles) {
                        mappedAuthorities.add(new SimpleGrantedAuthority(role.toUpperCase()));
                    }
                }
            }

            mappedAuthorities.addAll(oidcUser.getAuthorities());


            String username = oidcUser.getPreferredUsername(); // or .getEmail()
            // if you already saved a local user with this Keycloak sub, skip
            if (!userService.existsByUsername(username)) {
                // Create your domain objects however you like:
                // e.g. map oidcUser.getFullName() -> customer name
                Customer customer = customerService.createCustomer(
                        oidcUser.getFullName(), null);
                userService.createCustomerUser(
                        username,
                        // generate a random internal password or leave blank (Keycloak is the source of truth)
                        UUID.randomUUID().toString(),
                        customer
                );
            }

            return new DefaultOidcUser(
                    mappedAuthorities,
                    oidcUser.getIdToken(),
                    oidcUser.getUserInfo()
            );
        };
    }

}