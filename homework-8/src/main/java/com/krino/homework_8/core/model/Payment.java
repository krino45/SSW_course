package com.krino.homework_8.core.model;

import com.krino.homework_8.core.model.paymentType.Cash;
import com.krino.homework_8.core.model.paymentType.Check;
import com.krino.homework_8.core.model.paymentType.Credit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "payments")
@Getter
@Setter
public abstract class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private float amount;

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @Setter
    @Column(name = "payment_type")
    private String type;

    public String getType() {
        return this.getClass().getSimpleName().toUpperCase();
    }
}