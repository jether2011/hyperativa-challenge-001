package br.com.hyperativa.service.domain.entity.dto;

import java.util.Objects;

public record CardCreateDTO(String cardNumber) {
    private static final int CARD_NUMBER_LENGTH = 16;

    public CardCreateDTO {
        Objects.requireNonNull(cardNumber, "CardNumber cannot be null");

        if (cardNumber.isBlank()) {
            throw new IllegalArgumentException("CardNumber cannot be blank");
        }
    }

    public boolean isValidCardNumber() {
        return this.cardNumber.length() == CARD_NUMBER_LENGTH;
    }
}
