package br.com.hyperativa.service.application.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for encrypting and decrypting sensitive data using AES-256-GCM.
 * Provides secure encryption/decryption for PCI-compliant data storage.
 */
public class EncryptionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionUtil.class);
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private EncryptionUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Encrypts the given plaintext using AES-256-GCM.
     *
     * @param plaintext the text to encrypt
     * @param secret    the encryption key
     * @return Base64 encoded encrypted string with IV prepended
     */
    public static String encrypt(String plaintext, String secret) {
        try {
            SecretKey key = getKeyFromPassword(secret);
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            LOGGER.error("Encryption error", e);
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    /**
     * Decrypts the given encrypted text using AES-256-GCM.
     *
     * @param cipherText the Base64 encoded encrypted text
     * @param secret     the decryption key
     * @return decrypted plaintext
     */
    public static String decrypt(String cipherText, String secret) {
        try {
            SecretKey key = getKeyFromPassword(secret);
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            byte[] decoded = Base64.getDecoder().decode(cipherText);

            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] encryptedBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedBytes);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            byte[] plainText = cipher.doFinal(encryptedBytes);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("Decryption error", e);
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    /**
     * Derives a 256-bit AES key from the given password.
     *
     * @param password the password to derive the key from
     * @return SecretKey for AES encryption
     */
    private static SecretKey getKeyFromPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Key generation failed", e);
        }
    }
}
