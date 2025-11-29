package br.com.hyperativa.service.domain.services;

import br.com.hyperativa.service.domain.entity.dto.CardCreateDTO;
import br.com.hyperativa.service.domain.entity.dto.CardGetDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CardService {
    CardGetDTO createCard(final CardCreateDTO cardCreate);

    void createCardsInBatch(final List<CardCreateDTO> cardCreates);

    CardGetDTO getCardByNumber(final String cardNumber);

    CardGetDTO getCardByIdentifier(final String cardNumberIdentifier);

    Page<CardGetDTO> getAllCards(final Pageable pageable);
}
