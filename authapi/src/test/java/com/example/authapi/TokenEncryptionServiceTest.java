package com.example.authapi;

import com.example.authapi.service.TokenEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenEncryptionServiceTest {

    private TokenEncryptionService service;

    @BeforeEach
    void setUp() {
        service = new TokenEncryptionService("MySecretKey12345");
    }

    @Test
    void encrypt_then_decrypt_returns_original() {
        String plaintext = "alice-2024/02/14-19:59:32";
        String encrypted = service.encrypt(plaintext);
        String decrypted = service.decrypt(encrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void encrypt_produces_different_output_than_input() {
        String plaintext = "alice-2024/02/14-19:59:32";
        String encrypted = service.encrypt(plaintext);
        assertNotEquals(plaintext, encrypted);
    }

    @Test
    void two_different_inputs_produce_different_ciphertexts() {
        String enc1 = service.encrypt("alice-2024/01/01-10:00:00");
        String enc2 = service.encrypt("bob-2024/01/01-10:00:00");
        assertNotEquals(enc1, enc2);
    }

    @Test
    void same_input_produces_same_ciphertext() {
        // AES/ECB is deterministic
        String enc1 = service.encrypt("alice-2024/01/01-10:00:00");
        String enc2 = service.encrypt("alice-2024/01/01-10:00:00");
        assertEquals(enc1, enc2);
    }

    @Test
    void decrypt_invalid_input_throws() {
        assertThrows(RuntimeException.class, () -> service.decrypt("not_valid_base64!!!"));
    }
}
