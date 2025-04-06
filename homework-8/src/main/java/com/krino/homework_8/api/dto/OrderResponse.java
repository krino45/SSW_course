package com.krino.homework_8.api.dto;

import com.krino.homework_8.core.model.Order;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderResponse {
    private Long id;
    private LocalDateTime date;
    private String status;
    private CustomerDto customer;
    private List<OrderRequest.OrderDetailDto> orderDetails;
    private OrderRequest.PaymentDto payment;

    public static OrderResponse mapToDto(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.setId(order.getId());
        dto.setDate(order.getDate());
        dto.setStatus(order.getStatus());

        if (order.getCustomer() != null) {
            CustomerDto customerDto = new CustomerDto();
            customerDto.setId(order.getCustomer().getId());
            customerDto.setName(order.getCustomer().getName());
            dto.setCustomer(customerDto);
        }

        // Map Order Details
        List<OrderRequest.OrderDetailDto> orderDetailDtos = order.getOrderDetails().stream()
                .map(detail -> {
                    OrderRequest.QuantityDto quantityDto = new OrderRequest.QuantityDto();
                    quantityDto.setAmount(detail.getQuantity().getValue());
                    quantityDto.setUnit(detail.getQuantity().getName());
                    quantityDto.setUnitAbbreviation(detail.getQuantity().getSymbol());
                    OrderRequest.OrderDetailDto detailDto = new OrderRequest.OrderDetailDto();
                    detailDto.setItemId(detail.getItem().getId());
                    detailDto.setQuantity(quantityDto);
                    detailDto.setTaxStatus(detail.getTaxStatus());
                    return detailDto;
                })
                .collect(Collectors.toList());
        dto.setOrderDetails(orderDetailDtos);

        // Map Payment
        if (order.getPayment() != null) {
            OrderRequest.PaymentDto paymentDto = new OrderRequest.PaymentDto();
            paymentDto.setAmount(order.getPayment().getAmount());
            paymentDto.setStatus(order.getPayment().getStatus().name());
            paymentDto.setPaymentType(order.getPayment().getClass().getSimpleName().toUpperCase());
            dto.setPayment(paymentDto);
        }

        return dto;
    }

}