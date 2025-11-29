package br.com.hyperativa.service.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for file upload settings.
 */
@Configuration
public class FileUploadConfig {
    @Value("${app.file.upload.max-size:10485760}")
    private long maxFileSize;

    public long getMaxFileSize() {
        return maxFileSize;
    }
}
