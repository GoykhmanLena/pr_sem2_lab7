package ru.lenok.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Product {
    private final String name;
    private Long ownerId;
    private final Long id;
}
