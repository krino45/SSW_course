package com.krino.homework_8.core.model.paymentType;

import com.krino.homework_8.core.model.Payment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_payments")
@Getter
@Setter
public class Credit extends Payment {
    private String number;
    private String type;
    private LocalDateTime expDate;
}