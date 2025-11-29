package br.com.hyperativa.service.application.web.controller;

import br.com.hyperativa.service.application.web.controller.request.CardRequest;
import br.com.hyperativa.service.domain.entity.dto.CardCreateDTO;
import br.com.hyperativa.service.domain.entity.dto.CardGetDTO;
import br.com.hyperativa.service.domain.processor.Processor;
import br.com.hyperativa.service.domain.services.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for card management operations.
 * All endpoints require JWT authentication.
 */
@RestController
@RequestMapping("/v1/card")
@Tag(name = "Card Management", description = "Endpoints for secure card number storage and retrieval")
@SecurityRequirement(name = "Bearer Authentication")
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

    @Operation(summary = "Create a new card", description = "Stores a single card number securely with encryption")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Card created successfully",
                    content = @Content(schema = @Schema(implementation = CardGetDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid card number format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid")
    })
    @PostMapping("/create")
    public ResponseEntity<CardGetDTO> addCard(@RequestBody @Valid final CardRequest request) {
        final CardGetDTO card = cardService.createCard(new CardCreateDTO(request.cardNumber()));
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @Operation(summary = "Upload cards from file", description = "Batch upload card numbers from a TXT file following the specified format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "File accepted and processing started"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or content"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadCards(
            @Parameter(description = "TXT file with card numbers in the specified format")
            @RequestParam("file") MultipartFile file) {
        cardService.createCardsInBatch(processor.process(file));
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Get all cards", description = "Retrieve a paginated list of all stored cards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cards retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid")
    })
    @GetMapping
    public ResponseEntity<Page<CardGetDTO>> getCards(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

    @Operation(summary = "Get card by number", description = "Retrieve card information by its card number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card found",
                    content = @Content(schema = @Schema(implementation = CardGetDTO.class))),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid")
    })
    @GetMapping("/{cardNumber}")
    public ResponseEntity<CardGetDTO> getCard(
            @Parameter(description = "16-digit card number")
            @PathVariable final String cardNumber) {
        return ResponseEntity.ok(cardService.getCardByNumber(cardNumber));
    }
}
