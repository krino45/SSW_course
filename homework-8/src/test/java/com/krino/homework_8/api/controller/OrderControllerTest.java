package com.krino.homework_8.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krino.homework_8.core.model.Order;
import com.krino.homework_8.core.service.CustomerService;
import com.krino.homework_8.core.service.OrderService;
import com.krino.homework_8.core.config.UserSecurity;
import com.krino.homework_8.core.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class) // Explicitly specify the controller
@TestPropertySource(properties = { // Provide OAuth2 client properties
        "spring.security.oauth2.client.registration.spring-api.provider=spring-api",
        "spring.security.oauth2.client.registration.spring-api.client-id=test-client",
        "spring.security.oauth2.client.registration.spring-api.client-secret=test-secret",
        "spring.security.oauth2.client.registration.spring-api.authorization-grant-type=authorization_code"
})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserService userService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserSecurity userSecurity() {
            return Mockito.mock(UserSecurity.class);
        }
    }

    @Autowired
    private UserSecurity userSecurity;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository; // Mock OAuth2 client bean

    @Autowired
    private ObjectMapper objectMapper;

    private OAuth2AuthenticationToken userToken;
    private OAuth2AuthenticationToken adminToken;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        DefaultOAuth2User user = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", userId.toString()),
                "sub"
        );

        userToken = new OAuth2AuthenticationToken(user, List.of(new SimpleGrantedAuthority("ROLE_USER")), "spring-api");

        DefaultOAuth2User admin = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
                Map.of("sub", UUID.randomUUID().toString()),
                "sub"
        );

        adminToken = new OAuth2AuthenticationToken(admin, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), "spring-api");
        Mockito.reset(userSecurity);
    }

    @Test
    void shouldReturnOrdersForOwnCustomer() throws Exception {
        Order dummyOrder = new Order();
        Mockito.when(userSecurity.canAccessCustomer(any(), eq(1L))).thenReturn(true);
        Mockito.when(orderService.getOrdersByCustomerId(1L)).thenReturn(List.of(dummyOrder));

        mockMvc.perform(get("/orders/customer/1")
                        .with(authentication(userToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldDenyAccessWhenNotAuthorized() throws Exception {
        Mockito.when(userSecurity.canAccessCustomer(any(), eq(2L))).thenReturn(false);

        mockMvc.perform(get("/orders/customer/2")
                        .with(authentication(userToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldAccessAnyCustomer() throws Exception {
        Order dummyOrder = new Order();
        Mockito.when(orderService.getOrdersByCustomerId(2L)).thenReturn(List.of(dummyOrder));

        mockMvc.perform(get("/orders/customer/2")
                        .with(authentication(adminToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}