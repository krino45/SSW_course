package com.krino.homework_8.core.service;

import com.krino.homework_8.api.dto.OrderRequest;
import com.krino.homework_8.core.model.*;
import com.krino.homework_8.core.model.paymentType.Cash;
import com.krino.homework_8.core.model.paymentType.Check;
import com.krino.homework_8.core.model.paymentType.Credit;
import com.krino.homework_8.core.model.value.measurements.Quantity;
import com.krino.homework_8.core.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;
    private final PaymentRepository paymentRepository;

    public List<Order> searchOrders(
            String customerName,
            String city,
            String street,
            String zipcode,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String paymentType,
            String orderStatus,
            String paymentStatus) {

        Specification<Order> spec = Specification.where(null);

        // Customer name search
        if (customerName != null && !customerName.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("customer").get("name")), "%" + customerName.toLowerCase() + "%"));
        }

        // Address search
        if (city != null && !city.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("customer").get("address").get("city")), city.toLowerCase()));
        }

        if (street != null && !street.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("customer").get("address").get("street")), "%" + street.toLowerCase() + "%"));
        }

        if (zipcode != null && !zipcode.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("customer").get("address").get("zipcode"), zipcode));
        }

        // Date range search
        if (fromDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("date"), fromDate));
        }

        if (toDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("date"), toDate));
        }

        // Order status search
        if (orderStatus != null && !orderStatus.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), orderStatus));
        }

        // Payment type search (Cash, Check, Credit)
        if (paymentType != null && !paymentType.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                // Join with the payment table and check the type
                return cb.equal(
                        cb.treat(root.get("payment"), getPaymentClass(paymentType)).get("id"),
                        root.get("payment").get("id")
                );
            });
        }

        // Payment status search
        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                // Example: if we have a payment status field in the Payment entity
                return cb.equal(root.get("payment").get("status"), paymentStatus);
            });
        }

        return orderRepository.findAll(spec);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByCustomerId (Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    }

    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
        Customer customer = customerRepository.findById(orderRequest.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + orderRequest.getCustomerId()));

        Order order = new Order();
        order.setCustomer(customer);
        order.setDate(orderRequest.getDate() != null ? orderRequest.getDate() : LocalDateTime.now());
        order.setStatus(orderRequest.getStatus());

        // Process order details
        if (orderRequest.getOrderDetails() != null) {
            for (OrderRequest.OrderDetailDto detailDto : orderRequest.getOrderDetails()) {
                Optional<Item> maybe = itemRepository.findById(detailDto.getItemId());
                Item item = maybe.orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + detailDto.getItemId()));

                OrderDetail detail = getOrderDetail(detailDto, item);

                order.addOrderDetail(detail);
            }
        }

        orderRepository.save(order);

        // Process payment
        if (orderRequest.getPayment() != null) {
            Payment payment = createPaymentFromDto(orderRequest.getPayment(), order);
            order.setPayment(payment);
        }
        customer.addOrder(order);
        return orderRepository.save(order);
    }

    private static OrderDetail getOrderDetail(OrderRequest.OrderDetailDto detailDto, Item item) {
        OrderDetail detail = new OrderDetail();
        detail.setItem(item);
        detail.setTaxStatus(detailDto.getTaxStatus());

        if (detailDto.getQuantity() != null) {
            Quantity quantity = new Quantity(
                    detailDto.getQuantity().getAmount(),
                    detailDto.getQuantity().getUnit(),
                    detailDto.getQuantity().getUnitAbbreviation()
            );
            detail.setQuantity(quantity);
        }
        return detail;
    }

    @Transactional
    public Order updateOrder(Long id, OrderRequest orderRequest) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

        if (orderRequest.getStatus() != null) {
            order.setStatus(orderRequest.getStatus());
        }

        if (orderRequest.getDate() != null) {
            order.setDate(orderRequest.getDate());
        }

        if (orderRequest.getCustomerId() != null &&
                (order.getCustomer() == null || !order.getCustomer().getId().equals(orderRequest.getCustomerId()))) {

            Customer customer = customerRepository.findById(orderRequest.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + orderRequest.getCustomerId()));
            order.setCustomer(customer);
        }

        if (orderRequest.getOrderDetails() != null) {
            order.getOrderDetails().clear();

            for (OrderRequest.OrderDetailDto detailDto : orderRequest.getOrderDetails()) {
                Item item = itemRepository.findById(detailDto.getItemId())
                        .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + detailDto.getItemId()));

                OrderDetail detail = getDetail(detailDto, item);

                order.addOrderDetail(detail);
            }
        }

        updatePayment(order, orderRequest);

        return orderRepository.save(order);
    }

    private static OrderDetail getDetail(OrderRequest.OrderDetailDto detailDto, Item item) {
        OrderDetail detail = new OrderDetail();
        detail.setItem(item);
        detail.setTaxStatus(detailDto.getTaxStatus());

        if (detailDto.getQuantity() != null) {
            Quantity quantity = new Quantity(
                    detailDto.getQuantity().getAmount(),
                    detailDto.getQuantity().getUnit(),
                    detailDto.getQuantity().getUnitAbbreviation()
            );
            detail.setQuantity(quantity);
        }
        return detail;
    }

    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    private Payment createPaymentFromDto(OrderRequest.PaymentDto paymentDto, Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null when creating payment.");
        }
        log.info("Creating payment with order id {} for payment {}", order.getId(), paymentDto.getPaymentType());

        Payment payment;

        switch (paymentDto.getPaymentType().toUpperCase()) {
            case "CASH":
                Cash cashPayment = new Cash();
                if (paymentDto.getCashTendered() != null) {
                    cashPayment.setCashTendered(paymentDto.getCashTendered());
                }
                payment = cashPayment;
                break;

            case "CREDIT":
                Credit creditPayment = new Credit();
                creditPayment.setNumber(paymentDto.getCardNumber());
                creditPayment.setCardType(paymentDto.getCardType());
                creditPayment.setExpDate(paymentDto.getExpiryDate());
                payment = creditPayment;
                break;

            case "CHECK":
                Check checkPayment = new Check();
                checkPayment.setName(paymentDto.getName());
                checkPayment.setBankID(paymentDto.getBankId());
                payment = checkPayment;
                break;

            default:
                throw new IllegalArgumentException("Invalid payment type: " + paymentDto.getPaymentType());
        }

        payment.setAmount(paymentDto.getAmount());
        payment.setOrder(order);
        payment.setType(payment.getType().getClass().getSimpleName().toUpperCase());

        if (paymentDto.getStatus() != null) {
            payment.setStatus(PaymentStatus.valueOf(paymentDto.getStatus()));
        } else {
            payment.setStatus(PaymentStatus.PENDING);
        }
        paymentRepository.save(payment);
        return payment;
    }

    @Transactional
    private void updatePayment(Order order, OrderRequest orderRequest) {
        if (orderRequest.getPayment() == null) {
            return;
        }

        Payment existingPayment = order.getPayment();
        OrderRequest.PaymentDto paymentDto = orderRequest.getPayment();

        if (existingPayment != null) {
            // If payment type hasn't changed, just update fields
            if (existingPayment.getType().equals(getPaymentClass(paymentDto.getPaymentType()))) {
                updatePaymentFields(existingPayment, paymentDto);
                paymentRepository.save(existingPayment);
                return;
            }

            // If payment type changed, remove old payment first
            order.setPayment(null);  // Break the relationship
            paymentRepository.delete(existingPayment);
            paymentRepository.flush();  // Ensure deletion completes
        }

        Payment newPayment = createPaymentFromDto(paymentDto, order);
        newPayment.setType(newPayment.getType().getClass().getSimpleName().toUpperCase());
        paymentRepository.save(newPayment);
        order.setPayment(newPayment);
    }


    private void updatePaymentFields(Payment payment, OrderRequest.PaymentDto paymentDto) {
        payment.setAmount(paymentDto.getAmount());
        payment.setStatus(PaymentStatus.valueOf(paymentDto.getStatus()));

        if (payment instanceof Cash cash) {
            cash.setCashTendered(paymentDto.getCashTendered());
        } else if (payment instanceof Check check) {
            check.setName(paymentDto.getName());
            check.setBankID(paymentDto.getBankId());
        } else if (payment instanceof Credit credit) {
            credit.setNumber(paymentDto.getCardNumber());
            credit.setCardType(paymentDto.getCardType());
            credit.setExpDate(paymentDto.getExpiryDate());
        }

        paymentRepository.save(payment);
    }

    private Class<?> getPaymentClass(String paymentType) {
        return switch (paymentType.toLowerCase()) {
            case "cash" -> Cash.class;
            case "check" -> Check.class;
            case "credit" -> Credit.class;
            default -> Payment.class;
        };
    }
}



