package br.com.hyperativa.service.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for encryption settings.
 * Provides encryption key for sensitive data.
 */
@Configuration
public class EncryptionConfig {
    @Value("${app.encryption.key}")
    private String encryptionKey;

    public String getEncryptionKey() {
        return encryptionKey;
    }
}
