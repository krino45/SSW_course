package com.krino.homework_8.api.controller;

import com.krino.homework_8.api.dto.OrderRequest;
import com.krino.homework_8.api.dto.OrderResponse;
import com.krino.homework_8.core.model.Order;
import com.krino.homework_8.core.model.User;
import com.krino.homework_8.core.service.OrderService;
import com.krino.homework_8.core.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
@Slf4j
@EnableMethodSecurity
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders()
                .stream()
                .map(OrderResponse::mapToDto)
                .toList();
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.canAccessCustomer(authentication, #customerId)")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable Long customerId) {
        List<OrderResponse> orders = orderService.getOrdersByCustomerId(customerId)
                .stream()
                .map(OrderResponse::mapToDto)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.canAccessCustomer(authentication, #orderRequest.getCustomerId())")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        var order = orderService.createOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderResponse.mapToDto(order));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long id,
            @RequestBody OrderRequest orderRequest) {
        var updated = orderService.updateOrder(id, orderRequest);
        return ResponseEntity.ok(OrderResponse.mapToDto(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<OrderResponse>> searchOrders(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String street,
            @RequestParam(required = false) String zipcode,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate,
            @RequestParam(required = false) String paymentType,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String paymentStatus) {
        // Non-admin users will only see their own orders; service layer should enforce filtering based on caller's customer
        List<OrderResponse> results = orderService.searchOrders(
                        customerName, city, street, zipcode,
                        fromDate, toDate, paymentType, orderStatus, paymentStatus)
                .stream()
                .map(OrderResponse::mapToDto)
                .toList();

        return ResponseEntity.ok(results);
    }
}