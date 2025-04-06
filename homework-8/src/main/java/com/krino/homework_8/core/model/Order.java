package com.krino.homework_8.core.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;
    private String status;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ElementCollection
    @CollectionTable(
            name = "order_details",
            joinColumns = @JoinColumn(name = "order_id")
    )
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    @Nullable
    private Payment payment;

    public void setPayment(Payment payment) {
        this.payment = payment;
        if (payment != null)
            payment.setOrder(this);
    }

    public void addOrderDetail(OrderDetail detail1) {
        orderDetails.add(detail1);
    }
}