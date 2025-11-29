package br.com.hyperativa.service.domain.services.impl;

import br.com.hyperativa.service.domain.entity.Card;
import br.com.hyperativa.service.domain.entity.dto.CardCreateDTO;
import br.com.hyperativa.service.domain.entity.dto.CardGetDTO;
import br.com.hyperativa.service.domain.exceptions.CardCreateException;
import br.com.hyperativa.service.domain.exceptions.NotFoundException;
import br.com.hyperativa.service.resources.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardService Unit Tests")
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card testCard;
    private CardCreateDTO testCardCreateDTO;

    @BeforeEach
    void setUp() {
        testCard = new Card().cardNumber("1234567890123456");
        testCard.setId(1L);
        testCardCreateDTO = new CardCreateDTO("1234567890123456");
    }

    @Test
    @DisplayName("Should create card successfully")
    void shouldCreateCardSuccessfully() {
        // Given
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // When
        CardGetDTO result = cardService.createCard(testCardCreateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.cardNumberIdentifier()).isNotNull();
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    @DisplayName("Should throw CardCreateException when save fails")
    void shouldThrowCardCreateExceptionWhenSaveFails() {
        // Given
        when(cardRepository.save(any(Card.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> cardService.createCard(testCardCreateDTO))
                .isInstanceOf(CardCreateException.class)
                .hasMessageContaining("Card create error");
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    @DisplayName("Should create cards in batch successfully")
    void shouldCreateCardsInBatchSuccessfully() {
        // Given
        List<CardCreateDTO> cardCreateDTOs = Arrays.asList(
                new CardCreateDTO("1234567890123456"),
                new CardCreateDTO("9876543210987654")
        );
        when(cardRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        cardService.createCardsInBatch(cardCreateDTOs);

        // Then
        verify(cardRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should filter invalid cards in batch")
    void shouldFilterInvalidCardsInBatch() {
        // Given
        List<CardCreateDTO> cardCreateDTOs = Arrays.asList(
                new CardCreateDTO("1234567890123456"),
                new CardCreateDTO("123"), // Invalid - too short
                new CardCreateDTO("9876543210987654")
        );
        when(cardRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        cardService.createCardsInBatch(cardCreateDTOs);

        // Then
        verify(cardRepository, times(1)).saveAll(argThat(list -> list.size() == 2));
    }

    @Test
    @DisplayName("Should throw CardCreateException when batch save fails")
    void shouldThrowCardCreateExceptionWhenBatchSaveFails() {
        // Given
        List<CardCreateDTO> cardCreateDTOs = List.of(testCardCreateDTO);
        when(cardRepository.saveAll(anyList())).thenThrow(new RuntimeException("Batch error"));

        // When & Then
        assertThatThrownBy(() -> cardService.createCardsInBatch(cardCreateDTOs))
                .isInstanceOf(CardCreateException.class)
                .hasMessageContaining("Batch card create error");
    }

    @Test
    @DisplayName("Should get card by number successfully")
    void shouldGetCardByNumberSuccessfully() {
        // Given
        when(cardRepository.findByCardNumberHash(anyString())).thenReturn(Optional.of(testCard));

        // When
        CardGetDTO result = cardService.getCardByNumber("1234567890123456");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(cardRepository, times(1)).findByCardNumberHash(anyString());
    }

    @Test
    @DisplayName("Should throw NotFoundException when card not found by number")
    void shouldThrowNotFoundExceptionWhenCardNotFoundByNumber() {
        // Given
        when(cardRepository.findByCardNumberHash(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cardService.getCardByNumber("9999999999999999"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Card not found");
    }

    @Test
    @DisplayName("Should get card by identifier successfully")
    void shouldGetCardByIdentifierSuccessfully() {
        // Given
        String identifier = testCard.getCardNumberIdentifier();
        when(cardRepository.findByCardNumberIdentifier(identifier)).thenReturn(Optional.of(testCard));

        // When
        CardGetDTO result = cardService.getCardByIdentifier(identifier);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.cardNumberIdentifier()).isEqualTo(identifier);
        verify(cardRepository, times(1)).findByCardNumberIdentifier(identifier);
    }

    @Test
    @DisplayName("Should get all cards with pagination")
    void shouldGetAllCardsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<Card> cards = Arrays.asList(testCard, new Card().cardNumber("9876543210987654"));
        Page<Card> cardPage = new PageImpl<>(cards, pageable, cards.size());
        when(cardRepository.findAll(pageable)).thenReturn(cardPage);

        // When
        Page<CardGetDTO> result = cardService.getAllCards(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(cardRepository, times(1)).findAll(pageable);
    }
}
