package fr.gacha.authapi.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import fr.gacha.authapi.model.Token;
import fr.gacha.authapi.model.User;
import fr.gacha.authapi.repository.TokenRepository;
import fr.gacha.authapi.repository.UserRepository;

@Service
public class AuthService {

    private static final long TOKEN_TTL_SECONDS = 3600;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm:ss");

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final TokenEncryptionService encryptionService;

    public AuthService(UserRepository userRepository, TokenRepository tokenRepository,
            TokenEncryptionService encryptionService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.encryptionService = encryptionService;
    }

    // Registers a new user after verifying the username is not already taken
    public void register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }
        userRepository.save(new User(username, password));
    }

    // Returns AES-encrypted token built from "username-YYYY/MM/DD-HH:mm:ss"
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Revoke any existing active tokens
        tokenRepository.findAllByUserIdAndRevokedFalse(username).forEach(t -> {
            t.setRevoked(true);
            tokenRepository.save(t);
        });

        String plaintext = username + "-" + LocalDateTime.now().format(FORMATTER);
        String tokenValue = encryptionService.encrypt(plaintext);

        Instant now = Instant.now();
        tokenRepository.save(new Token(username, tokenValue, now, now.plusSeconds(TOKEN_TTL_SECONDS), false));

        return tokenValue;
    }

    // Returns the username linked to the token, slides expiry +1h, or throws on invalid/expired
    public String validate(String tokenValue) {
        Token token = tokenRepository.findByValueAndRevokedFalse(tokenValue)
                .orElseThrow(() -> new SecurityException("Invalid token"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new SecurityException("Token expired");
        }

        // Slide expiry
        token.setExpiresAt(Instant.now().plusSeconds(TOKEN_TTL_SECONDS));
        tokenRepository.save(token);

        return token.getUserId();
    }
}
