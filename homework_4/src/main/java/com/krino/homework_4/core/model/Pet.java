package com.krino.homework_4.core.model.enums;

import com.krino.homework_4.core.model.Category;
import com.krino.homework_4.core.model.Tag;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pet {
    Integer id;
    String name;
    Category category;
    List<Tag> tags;
    Status status;
}
