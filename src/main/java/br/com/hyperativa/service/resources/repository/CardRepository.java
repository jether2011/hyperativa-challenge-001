package br.com.hyperativa.service.resources.repository;

import br.com.hyperativa.service.domain.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Card entity.
 * Uses card_number_hash for efficient searching without decryption.
 */
public interface CardRepository extends JpaRepository<Card, Long> {
    /**
     * Find card by SHA-256 hash of card number.
     * More efficient than decrypting all records.
     */
    Optional<Card> findByCardNumberHash(final String cardNumberHash);

    Optional<Card> findByCardNumberIdentifier(final String cardNumberIdentifier);
}
