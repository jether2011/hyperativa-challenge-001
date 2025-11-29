package br.com.hyperativa.service.domain.entity;

import br.com.hyperativa.service.application.config.CardNumberEncryptor;
import io.azam.ulidj.ULID;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Entity representing a credit/debit card.
 * Card numbers are automatically encrypted at rest using AES-256-GCM.
 */
@Entity
@Table(name = "card")
public class Card extends BaseEntity {
    private static final int CARD_NUMBER_LENGTH = 16;
    private static final int CARD_NUMBER_IDENTIFIER_LENGTH = 26;
    private static final int ENCRYPTED_CARD_NUMBER_LENGTH = 255;

    @NotBlank(message = "Card number cannot be blank")
    @Size(min = CARD_NUMBER_LENGTH, max = CARD_NUMBER_LENGTH, message = "Card number must be exactly 16 digits")
    @Convert(converter = CardNumberEncryptor.class)
    @Column(name = "card_number", nullable = false, length = ENCRYPTED_CARD_NUMBER_LENGTH)
    private String cardNumber;

    /**
     * SHA-256 hash of the card number for search purposes.
     * Allows searching without decrypting all records.
     * Unique constraint ensures no duplicate cards.
     */
    @Column(name = "card_number_hash", nullable = false, unique = true, length = 64)
    private String cardNumberHash;

    @NotBlank(message = "Card number identifier cannot be blank")
    @Size(max = CARD_NUMBER_IDENTIFIER_LENGTH, message = "Card number identifier must not exceed 26 characters")
    @Column(name = "card_number_identifier", nullable = false, unique = true, length = CARD_NUMBER_IDENTIFIER_LENGTH)
    private String cardNumberIdentifier;

    public Card cardNumber(final String cardNumber) {
        this.cardNumber = cardNumber;
        this.cardNumberHash = generateHash(cardNumber);
        this.cardNumberIdentifier = ULID.random();
        return this;
    }

    /**
     * Generates SHA-256 hash of card number for search purposes.
     */
    private String generateHash(String cardNumber) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(cardNumber.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate hash", e);
        }
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardNumberHash() {
        return cardNumberHash;
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
