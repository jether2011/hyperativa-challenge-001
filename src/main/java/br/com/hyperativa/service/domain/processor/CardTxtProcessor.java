package br.com.hyperativa.service.domain.processor;

import br.com.hyperativa.service.application.config.FileUploadConfig;
import br.com.hyperativa.service.domain.entity.dto.CardCreateDTO;
import br.com.hyperativa.service.domain.exceptions.FileUploadException;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Processor for parsing card numbers from TXT files.
 * File format expected:
 * - Header line: DESAFIO-HYPERATIVA + date + LOTE info (51 chars)
 * - Card lines: Identifier (C1-CN) + card number (7-26 position)
 * - Footer line: LOTE + count
 */
@Component
public class CardTxtProcessor implements Processor<MultipartFile, List<CardCreateDTO>> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CardTxtProcessor.class);
    private static final String HEADER_WORD = "LOTE";
    private static final int LINE_LENGTH = 26;
    private static final int HEADER_LENGTH = 51;
    private static final String CONTENT_TYPE = "text/plain";
    private static final int CARD_NUMBER_START = 7;
    private static final int CARD_NUMBER_END = 26;

    private final FileUploadConfig fileUploadConfig;

    public CardTxtProcessor(FileUploadConfig fileUploadConfig) {
        this.fileUploadConfig = fileUploadConfig;
    }

    @Override
    public List<CardCreateDTO> process(MultipartFile input) throws FileUploadException {
        validateFile(input);

        final List<CardCreateDTO> cards = new ArrayList<>();
        int lineNumber = 0;
        int validCards = 0;
        int invalidCards = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input.getInputStream(), StandardCharsets.UTF_8))) {
            String line;

            // Validate header
            lineNumber++;
            String header = reader.readLine();
            if (header == null || header.length() < HEADER_LENGTH) {
                throw new FileUploadException(
                        String.format("Invalid file format: header at line %d is missing or too short (expected %d chars, got %d)",
                                lineNumber, HEADER_LENGTH, header != null ? header.length() : 0));
            }

            // Process card lines
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Footer line indicates end of cards
                if (line.startsWith(HEADER_WORD)) {
                    LOGGER.info("Footer found at line {}, stopping processing", lineNumber);
                    break;
                }

                // Skip lines that are too short
                if (line.length() < LINE_LENGTH) {
                    LOGGER.warn("Line {} is too short (length: {}), skipping", lineNumber, line.length());
                    continue;
                }

                try {
                    String cardNumber = line.substring(CARD_NUMBER_START, Math.min(CARD_NUMBER_END, line.length())).trim();

                    if (!cardNumber.isEmpty()) {
                        cards.add(new CardCreateDTO(cardNumber));
                        validCards++;
                    }
                } catch (Exception e) {
                    invalidCards++;
                    LOGGER.error("Failed to parse card at line {}: {}", lineNumber, e.getMessage());
                }
            }

            LOGGER.info("File processing completed: {} valid cards, {} invalid cards from {} total lines",
                    validCards, invalidCards, lineNumber);

            if (cards.isEmpty()) {
                throw new FileUploadException("No valid card numbers found in file");
            }

        } catch (FileUploadException e) {
            throw e;
        } catch (Exception e) {
            throw new FileUploadException(
                    String.format("File processing error at line %d: %s", lineNumber, e.getMessage()), e);
        }

        return cards;
    }

    /**
     * Validates file before processing.
     *
     * @param file the file to validate
     * @throws FileUploadException if validation fails
     */
    private void validateFile(MultipartFile file) throws FileUploadException {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is empty or null");
        }

        if (file.getSize() > fileUploadConfig.getMaxFileSize()) {
            throw new FileUploadException(
                    String.format("File size (%d bytes) exceeds maximum allowed size (%d bytes)",
                            file.getSize(), fileUploadConfig.getMaxFileSize()));
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.equals(CONTENT_TYPE) && !contentType.contains("text")) {
            LOGGER.warn("File content type '{}' is not text/plain, but will attempt to process", contentType);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.toLowerCase().endsWith(".txt")) {
            LOGGER.warn("File extension is not .txt: {}", originalFilename);
        }
    }
}
