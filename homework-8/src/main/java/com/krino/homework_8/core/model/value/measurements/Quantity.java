package com.krino.homework_8.core.model.value.measurements;

import com.krino.homework_8.core.model.value.Measurement;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Quantity implements Measurement {
    @Column(name = "quantity_value")
    private Integer value;
    @Column(name = "quantity_name")
    private String name;
    @Column(name = "quantity_symbol")
    private String symbol;
}