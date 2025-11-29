package br.com.hyperativa.service.domain.services.impl;

import br.com.hyperativa.service.domain.entity.Card;
import br.com.hyperativa.service.domain.entity.dto.CardCreateDTO;
import br.com.hyperativa.service.domain.entity.dto.CardGetDTO;
import br.com.hyperativa.service.domain.exceptions.CardCreateException;
import br.com.hyperativa.service.domain.exceptions.NotFoundException;
import br.com.hyperativa.service.domain.services.CardService;
import br.com.hyperativa.service.resources.repository.CardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for managing card operations.
 * Handles creation, retrieval, and batch processing of card data.
 */
@Service
@Transactional(readOnly = true)
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;

    public CardServiceImpl(final CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    @Transactional
    public CardGetDTO createCard(final CardCreateDTO cardCreate) {
        try {
            final Card created = cardRepository.save(new Card().cardNumber(cardCreate.cardNumber()));
            return new CardGetDTO(created.getId(), created.getCardNumberIdentifier());
        } catch (Exception e) {
            throw new CardCreateException("Card create error", e);
        }
    }

    @Override
    @Transactional
    public void createCardsInBatch(List<CardCreateDTO> cardCreates) {
        try {
            final List<Card> cards = cardCreates.stream()
                    .filter(CardCreateDTO::isValidCardNumber)
                    .map(cardCreate -> new Card().cardNumber(cardCreate.cardNumber()))
                    .toList();
            cardRepository.saveAll(cards);
        } catch (Exception e) {
            throw new CardCreateException("Batch card create error", e);
        }
    }

    @Override
    public CardGetDTO getCardByNumber(final String cardNumber) {
        String hash = hashCardNumber(cardNumber);
        return cardRepository.findByCardNumberHash(hash)
                .map(card -> new CardGetDTO(card.getId(), card.getCardNumberIdentifier()))
                .orElseThrow(() -> new NotFoundException("Card not found"));
    }

    /**
     * Generates SHA-256 hash of card number for search purposes.
     * Must match the hash generation in Card entity.
     */
    private String hashCardNumber(String cardNumber) {
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

    @Override
    public CardGetDTO getCardByIdentifier(final String cardNumberIdentifier) {
        return cardRepository.findByCardNumberIdentifier(cardNumberIdentifier)
                .map(card -> new CardGetDTO(card.getId(), card.getCardNumberIdentifier()))
                .orElseThrow(() -> new NotFoundException("Card not found"));
    }

    @Override
    public Page<CardGetDTO> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(card -> new CardGetDTO(card.getId(), card.getCardNumberIdentifier()));
    }
}
