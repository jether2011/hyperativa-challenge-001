package br.com.hyperativa.service.application.web.controller;

import br.com.hyperativa.service.domain.entity.dto.CardCreateDTO;
import br.com.hyperativa.service.domain.entity.dto.CardGetDTO;
import br.com.hyperativa.service.domain.processor.Processor;
import br.com.hyperativa.service.domain.services.CardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/card")
public class CardController {
    private final CardService cardService;

    private final Processor<MultipartFile, List<CardCreateDTO>> processor;

    public CardController(
            final CardService cardService,
            final Processor<MultipartFile, List<CardCreateDTO>> processor
    ) {
        this.cardService = cardService;
        this.processor = processor;
    }

    @PostMapping("/create")
    public ResponseEntity<CardGetDTO> addCard(@RequestBody @Valid final CardRequest request) {
        final CardGetDTO card = cardService.createCard(new CardCreateDTO(request.cardNumber()));
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @PostMapping("/upload")
    public ResponseEntity<Void> uploadCards(@RequestParam("file") MultipartFile file) {
        cardService.createCardsInBatch(processor.process(file));

        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public ResponseEntity<Page<CardGetDTO>> getCards(@PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

    @GetMapping("/{cardNumber}")
    public ResponseEntity<CardGetDTO> getCard(@PathVariable final String cardNumber) {
        return ResponseEntity.ok(cardService.getCardByNumber(cardNumber));
    }
}

record CardRequest(@NotBlank @Size(max = 16, min = 16) String cardNumber) {
}
