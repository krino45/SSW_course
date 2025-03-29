package com.krino.homework_7.core.model.value.measurements;

import com.krino.homework_7.core.model.value.Measurement;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
public class Weight implements Measurement {
    private BigDecimal value;
    private String name;
    private String symbol;
}