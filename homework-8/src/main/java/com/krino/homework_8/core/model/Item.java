package com.krino.homework_8.core.model;

import com.krino.homework_8.core.model.value.measurements.Weight;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "items")
@Getter
@Setter
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Weight shippingWeight;

    private String description;

    @ElementCollection
    @CollectionTable(
            name = "order_details",
            joinColumns = @JoinColumn(name = "order_id")
    )
    private List<OrderDetail> orderDetails = new ArrayList<>();
}
