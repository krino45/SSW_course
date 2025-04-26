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
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(Authentication authentication) {
        if (authentication == null) {
            log.info("Authentication is null @ getAllOrders");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = (User) authentication.getPrincipal();
        if (!userService.hasAdminAccess(user)) {
            log.info("Has no admin rights @ getAllOrders");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders.stream().map(OrderResponse::mapToDto).toList());
    }

        @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(
            @PathVariable Long customerId,
            Authentication authentication) {
        if (authentication == null) {
            log.info("Authentication is null @ getOrdersByCustomer");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = (User) authentication.getPrincipal();

        if (!userService.canAccessCustomer(user, customerId)) {
            log.info("Customer {} cant access orders for {} @ getOrdersByCustomer", user.getCustomer().getId(), customerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Order> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders.stream().map(OrderResponse::mapToDto).toList());
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody OrderRequest orderRequest,
            Authentication authentication) {
        if (authentication == null) {
            log.info("Authentication is null @ createOrder");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = (User) authentication.getPrincipal();

        if (!userService.canAccessCustomer(user, orderRequest.getCustomerId())) {
            log.info("Customer {} cant create orders for {} @ getOrdersByCustomer", user.getCustomer().getId(), orderRequest.getCustomerId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Order order = orderService.createOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.mapToDto(order));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long id,
            @RequestBody OrderRequest orderRequest,
            Authentication authentication) {
        if (authentication == null) {
            log.info("Authentication is null @ updateOrder");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = (User) authentication.getPrincipal();

        if (!userService.hasAdminAccess(user)) {
            log.info("User is not an admin @ updateOrder");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Order order = orderService.updateOrder(id, orderRequest);
        return ResponseEntity.ok(OrderResponse.mapToDto(order));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long id,
            Authentication authentication) {
        if (authentication == null) {
            log.info("Authentication is null @ deleteOrder");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = (User) authentication.getPrincipal();

        if (!userService.hasAdminAccess(user)) {
            log.info("User is not an admin @ deleteOrder");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrderResponse>> searchOrders(@RequestParam(required = false) String customerName,
                                                            @RequestParam(required = false) String city,
                                                            @RequestParam(required = false) String street,
                                                            @RequestParam(required = false) String zipcode,
                                                            @RequestParam(required = false) LocalDateTime fromDate,
                                                            @RequestParam(required = false) LocalDateTime toDate,
                                                            @RequestParam(required = false) String paymentType,
                                                            @RequestParam(required = false) String orderStatus,
                                                            @RequestParam(required = false) String paymentStatus,
                                                            Authentication authentication
                                                                ) {
        if (authentication == null) {
            log.info("Authentication is null @ searchOrders");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = (User) authentication.getPrincipal();

        // Only admin can delete orders
        if (!userService.hasAdminAccess(user) && customerName != null && !Objects.equals(customerName, user.getCustomer().getName()) ) {
            log.info("Customer {} ({}) cant search orders for {} @ searchOrders", user.getCustomer().getId(), user.getCustomer().getName(), customerName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } else if (!userService.hasAdminAccess(user) && customerName == null) {
            customerName = user.getCustomer().getName();
        }

        return ResponseEntity.ok(
                orderService.searchOrders(customerName, city, street, zipcode, fromDate, toDate, paymentType, orderStatus, paymentStatus)
                        .stream().map(OrderResponse::mapToDto).toList()
        );
    }
}