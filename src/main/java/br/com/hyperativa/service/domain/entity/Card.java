package br.com.hyperativa.service.domain.entity;

import io.azam.ulidj.ULID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.Objects;

@Entity
@Table(name = "card")
public class Card extends BaseEntity {
    private static final int CARD_NUMBER_LENGTH = 16;
    private static final int CARD_NUMBER_IDENTIFIER_LENGTH = 26;

    @Max(CARD_NUMBER_LENGTH)
    @Min(CARD_NUMBER_LENGTH)
    @Column(name = "card_number", nullable = false, unique = true, length = CARD_NUMBER_LENGTH)
    private String cardNumber;

    @Column(name = "card_number_identifier", nullable = false, unique = true, length = CARD_NUMBER_IDENTIFIER_LENGTH)
    private String cardNumberIdentifier;

    public Card cardNumber(final String cardNumber) {
        this.cardNumber = cardNumber;
        this.cardNumberIdentifier = ULID.random();
        return this;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardNumberIdentifier() {
        return cardNumberIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Card card)) return false;
        return Objects.equals(cardNumber, card.cardNumber) &&
                Objects.equals(this.getId(), card.getId()) &&
                Objects.equals(cardNumberIdentifier, card.cardNumberIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), cardNumber, cardNumberIdentifier);
    }

    @Override
    public String toString() {
        return "Card{" +
                "id='" + this.getId() + '\'' +
                "cardNumber='" + cardNumber + '\'' +
                ", cardNumberIdentifier='" + cardNumberIdentifier + '\'' +
                '}';
    }
}
