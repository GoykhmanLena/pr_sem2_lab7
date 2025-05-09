package ru.lenok.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FullOffer {
    private final Long id;
    private final Long labWorkId;
    private final String labWorkName;
    private final Long productId;
    private final String productName;
    private final Long ownerId;
    private final String ownerName; //labWork или product owner
    private final OfferStatus offerStatus;
}
