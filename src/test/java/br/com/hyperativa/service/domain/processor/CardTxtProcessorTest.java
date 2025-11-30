package br.com.hyperativa.service.domain.processor;

import br.com.hyperativa.service.application.config.FileUploadConfig;
import br.com.hyperativa.service.domain.entity.dto.CardCreateDTO;
import br.com.hyperativa.service.domain.exceptions.FileUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardTxtProcessor Unit Tests")
class CardTxtProcessorTest {

    @Mock
    private FileUploadConfig fileUploadConfig;

    private CardTxtProcessor processor;

    @BeforeEach
    void setUp() {
        lenient().when(fileUploadConfig.getMaxFileSize()).thenReturn(10485760L); // 10MB
        processor = new CardTxtProcessor(fileUploadConfig);
    }

    @Test
    @DisplayName("Should process valid file successfully")
    void shouldProcessValidFileSuccessfully() {
        // Given - Format matches cards_upload_file.txt (padded to 51 chars)
        String fileContent = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000010
                C1     4456897922969999
                C2     4456897999999999
                LOTE0001000002
                """;
        MultipartFile file = new MockMultipartFile(
                "file",
                "cards.txt",
                "text/plain",
                fileContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<CardCreateDTO> result = processor.process(file);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).cardNumber()).isEqualTo("4456897922969999");
        assertThat(result.get(1).cardNumber()).isEqualTo("4456897999999999");
    }

    @Test
    @DisplayName("Should throw exception for empty file")
    void shouldThrowExceptionForEmptyFile() {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                "cards.txt",
                "text/plain",
                new byte[0]
        );

        // When & Then
        assertThatThrownBy(() -> processor.process(file))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("File is empty");
    }

    @Test
    @DisplayName("Should throw exception for file exceeding max size")
    void shouldThrowExceptionForFilExceedingMaxSize() {
        // Given
        when(fileUploadConfig.getMaxFileSize()).thenReturn(10L); // Very small limit
        String fileContent = "DESAFIO-HYPERATIVA           20180524LOTE0001000010\nC1     4456897922969999                          \nLOTE0001000001                                        \n";
        MultipartFile file = new MockMultipartFile(
                "file",
                "cards.txt",
                "text/plain",
                fileContent.getBytes(StandardCharsets.UTF_8)
        );

        // When & Then
        assertThatThrownBy(() -> processor.process(file))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("exceeds maximum allowed size");
    }

    @Test
    @DisplayName("Should throw exception for invalid header")
    void shouldThrowExceptionForInvalidHeader() {
        // Given
        String fileContent = "SHORT\nC1     4456897922969999\n";
        MultipartFile file = new MockMultipartFile(
                "file",
                "cards.txt",
                "text/plain",
                fileContent.getBytes(StandardCharsets.UTF_8)
        );

        // When & Then
        assertThatThrownBy(() -> processor.process(file))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("header");
    }

    @Test
    @DisplayName("Should throw exception when no valid cards found")
    void shouldThrowExceptionWhenNoValidCardsFound() {
        // Given
        String fileContent = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000010
                LOTE0001000000
                """;
        MultipartFile file = new MockMultipartFile(
                "file",
                "cards.txt",
                "text/plain",
                fileContent.getBytes(StandardCharsets.UTF_8)
        );

        // When & Then
        assertThatThrownBy(() -> processor.process(file))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("No valid card numbers found");
    }

    @Test
    @DisplayName("Should skip lines that are too short")
    void shouldSkipLinesThatAreTooShort() {
        // Given
        String fileContent = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000010
                SHORT
                C1     4456897922969999
                LOTE0001000001
                """;
        MultipartFile file = new MockMultipartFile(
                "file",
                "cards.txt",
                "text/plain",
                fileContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<CardCreateDTO> result = processor.process(file);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).cardNumber()).isEqualTo("4456897922969999");
    }

    @Test
    @DisplayName("Should handle file with only valid 16-digit cards")
    void shouldHandleFileWithOnlyValid16DigitCards() {
        // Given
        String fileContent = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000010
                C1     4456897922969999
                C2     1234567890123456
                C3     9876543210987654
                LOTE0001000003
                """;
        MultipartFile file = new MockMultipartFile(
                "file",
                "cards.txt",
                "text/plain",
                fileContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<CardCreateDTO> result = processor.process(file);

        // Then
        assertThat(result).hasSize(3);
    }
}
