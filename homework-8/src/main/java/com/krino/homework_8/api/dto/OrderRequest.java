package com.krino.homework_8.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderRequest {
    private Long id;
    private Long customerId;
    private LocalDateTime date;
    private String status;
    private List<OrderDetailDto> orderDetails;
    private PaymentDto payment;

    @Data
    public static class OrderDetailDto {
        private Long itemId;
        private String taxStatus;
        private QuantityDto quantity;
    }

    @Data
    public static class QuantityDto {
        private int amount;
        private String unit;
        private String unitAbbreviation;
    }

    @Data
    public static class PaymentDto {
        private float amount;
        private String paymentType;
        private String status;

        private Float cashTendered;

        private String cardNumber;
        private String cardType;
        private LocalDateTime expiryDate;

        private String name;
        private String bankId;
    }
}