package fr.gacha.authapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import fr.gacha.authapi.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Crée un nouveau compte utilisateur avec le username et le mot de passe fournis. */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        authService.register(body.get("username"), body.get("password"));
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    /** Authentifie l'utilisateur et retourne un token chiffré AES valable 1 heure. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String token = authService.login(body.get("username"), body.get("password"));
            return ResponseEntity.ok(Map.of("token", token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    /** Vérifie le token Bearer et retourne le username associé ; prolonge la durée de vie de +1h. */
    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing or malformed token"));
        }
        try {
            String username = authService.validate(authHeader.substring(7));
            return ResponseEntity.ok(Map.of("username", username));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }
}
