package fr.gacha.authapi;

import fr.gacha.authapi.model.Token;
import fr.gacha.authapi.model.User;
import fr.gacha.authapi.repository.TokenRepository;
import fr.gacha.authapi.repository.UserRepository;
import fr.gacha.authapi.service.AuthService;
import fr.gacha.authapi.service.TokenEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthapiApplicationTests {

    @Mock private UserRepository userRepository;
    @Mock private TokenRepository tokenRepository;
    @Mock private TokenEncryptionService encryptionService;

    private AuthService authService;
    private TokenEncryptionService realEncryptionService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, tokenRepository, encryptionService);
        realEncryptionService = new TokenEncryptionService("MySecretKey12345");
    }

    // ── register ──────────────────────────────────────────────────────

    @Test
    void register_success_saves_user() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);

        authService.register("alice", "password123");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicate_username_throws() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> authService.register("alice", "password123"));
        verify(userRepository, never()).save(any());
    }

    // ── login ─────────────────────────────────────────────────────────

    @Test
    void login_valid_credentials_returns_token() {
        User user = new User("alice", "password123");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(tokenRepository.findAllByUserIdAndRevokedFalse("alice")).thenReturn(Collections.emptyList());
        when(encryptionService.encrypt(any())).thenReturn("encrypted_token");
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String token = authService.login("alice", "password123");

        assertEquals("encrypted_token", token);
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void login_user_not_found_throws() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> authService.login("unknown", "password"));
    }

    @Test
    void login_wrong_password_throws() {
        User user = new User("alice", "correct_password");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> authService.login("alice", "wrong_password"));
    }

    @Test
    void login_revokes_existing_tokens() {
        User user = new User("alice", "password123");
        Token existingToken = new Token("alice", "old_token", Instant.now(), Instant.now().plusSeconds(3600), false);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(tokenRepository.findAllByUserIdAndRevokedFalse("alice")).thenReturn(Collections.singletonList(existingToken));
        when(encryptionService.encrypt(any())).thenReturn("new_token");
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.login("alice", "password123");

        assertTrue(existingToken.isRevoked());
    }

    // ── validate ──────────────────────────────────────────────────────

    @Test
    void validate_valid_token_returns_username() {
        Token token = new Token("alice", "encrypted_token",
                Instant.now().minusSeconds(10),
                Instant.now().plusSeconds(3590),
                false);
        when(tokenRepository.findByValueAndRevokedFalse("encrypted_token")).thenReturn(Optional.of(token));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String username = authService.validate("encrypted_token");

        assertEquals("alice", username);
    }

    @Test
    void validate_expired_token_throws() {
        Token token = new Token("alice", "expired_token",
                Instant.now().minusSeconds(7200),
                Instant.now().minusSeconds(3600),
                false);
        when(tokenRepository.findByValueAndRevokedFalse("expired_token")).thenReturn(Optional.of(token));

        assertThrows(SecurityException.class, () -> authService.validate("expired_token"));
    }

    @Test
    void validate_unknown_token_throws() {
        when(tokenRepository.findByValueAndRevokedFalse("unknown")).thenReturn(Optional.empty());

        assertThrows(SecurityException.class, () -> authService.validate("unknown"));
    }

    @Test
    void validate_slides_expiry_by_one_hour() {
        Instant originalExpiry = Instant.now().plusSeconds(30);
        Token token = new Token("alice", "token",
                Instant.now().minusSeconds(10),
                originalExpiry,
                false);
        when(tokenRepository.findByValueAndRevokedFalse("token")).thenReturn(Optional.of(token));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.validate("token");

        assertTrue(token.getExpiresAt().isAfter(originalExpiry));
    }

    // ── TokenEncryptionService ─────────────────────────────────────────

    @Test
    void encrypt_then_decrypt_returns_original() {
        String plaintext = "alice-2024/02/14-19:59:32";
        String encrypted = realEncryptionService.encrypt(plaintext);
        String decrypted = realEncryptionService.decrypt(encrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void encrypt_produces_different_output_than_input() {
        String plaintext = "alice-2024/02/14-19:59:32";
        String encrypted = realEncryptionService.encrypt(plaintext);
        assertNotEquals(plaintext, encrypted);
    }

    @Test
    void two_different_inputs_produce_different_ciphertexts() {
        String enc1 = realEncryptionService.encrypt("alice-2024/01/01-10:00:00");
        String enc2 = realEncryptionService.encrypt("bob-2024/01/01-10:00:00");
        assertNotEquals(enc1, enc2);
    }

    @Test
    void same_input_produces_same_ciphertext() {
        String enc1 = realEncryptionService.encrypt("alice-2024/01/01-10:00:00");
        String enc2 = realEncryptionService.encrypt("alice-2024/01/01-10:00:00");
        assertEquals(enc1, enc2);
    }

    @Test
    void decrypt_invalid_input_throws() {
        assertThrows(RuntimeException.class, () -> realEncryptionService.decrypt("not_valid_base64!!!"));
    }
}
