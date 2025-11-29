package br.com.hyperativa.service.domain.processor;

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

@Component
public class CardTxtProcessor implements Processor<MultipartFile, List<CardCreateDTO>> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CardTxtProcessor.class);
    private static final String HEADER_WORD = "LOTE";
    private static final int LINE_LENGTH = 26;
    private static final int HEADER_LENGTH = 51;

    @Override
    public List<CardCreateDTO> process(MultipartFile input) throws FileUploadException {
        final List<CardCreateDTO> cards = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input.getInputStream(), StandardCharsets.UTF_8))) {
            String line;

            String header = reader.readLine();
            if (header == null || header.length() < HEADER_LENGTH) {
                throw new FileUploadException("Invalid file format: header is missing or too short.");
            }

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(HEADER_WORD)) {
                    break;
                }

                if (line.length() < LINE_LENGTH) {
                    continue;
                }

                String cardNumber = line.substring(7, 26).trim();

                try {
                    cards.add(new CardCreateDTO(cardNumber));
                } catch (Exception e) {
                    LOGGER.error("Card number is invalid: {}", cardNumber, e);
                }
            }
        } catch (Exception e) {
            throw new FileUploadException("File upload error", e);
        }

        return cards;
    }
}
