package br.com.hyperativa.service.resources.repository;

import br.com.hyperativa.service.domain.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardNumber(final String cardNumber);
    Optional<Card> findByCardNumberIdentifier(final String cardNumberIdentifier);
}
