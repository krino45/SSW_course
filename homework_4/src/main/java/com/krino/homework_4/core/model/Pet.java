package com.krino.homework_4.core.model;

import com.krino.homework_4.core.model.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pet {
    Integer id;
    @NotNull(message = "Name cannot be null")
    String name;
    Category category;
    List<Tag> tags;
    Status status;
}
