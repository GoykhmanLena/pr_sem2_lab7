package ru.lenok.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Offer {
    private final Long labWorkId;
    private final Long productId;
    private OfferStatus status;
    private final Long id;
}
