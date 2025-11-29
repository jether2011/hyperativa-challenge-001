package br.com.hyperativa.service.application.config;

import br.com.hyperativa.service.application.util.EncryptionUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter for automatic encryption/decryption of card numbers.
 * Ensures card numbers are encrypted at rest in the database.
 */
@Converter
@Component
public class CardNumberEncryptor implements AttributeConverter<String, String> {
    private final EncryptionConfig encryptionConfig;

    @Autowired
    public CardNumberEncryptor(EncryptionConfig encryptionConfig) {
        this.encryptionConfig = encryptionConfig;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return EncryptionUtil.encrypt(attribute, encryptionConfig.getEncryptionKey());
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return EncryptionUtil.decrypt(dbData, encryptionConfig.getEncryptionKey());
    }
}
