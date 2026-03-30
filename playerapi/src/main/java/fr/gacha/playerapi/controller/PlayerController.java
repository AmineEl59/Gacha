package fr.gacha.playerapi.controller;

import fr.gacha.playerapi.dto.CreateMonsterRequest;
import fr.gacha.playerapi.dto.ExperienceRequest;
import fr.gacha.playerapi.dto.LinkMonsterRequest;
import fr.gacha.playerapi.model.Player;
import fr.gacha.playerapi.service.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    // Creates a new player profile for the currently authenticated user
    @PostMapping
    public ResponseEntity<Player> createPlayer(Authentication auth) {
        Player player = playerService.createPlayer(auth.getName());
        return ResponseEntity.ok(player);
    }

    /** Get full profile. */
    @GetMapping("/{username}")
    public ResponseEntity<Player> getProfile(@PathVariable String username) {
        return ResponseEntity.ok(playerService.getProfile(username));
    }

    /** Get list of monster IDs. */
    @GetMapping("/{username}/monsters")
    public ResponseEntity<List<String>> getMonsters(@PathVariable String username) {
        return ResponseEntity.ok(playerService.getMonsters(username));
    }

    /** Get current level. */
    @GetMapping("/{username}/level")
    public ResponseEntity<Map<String, Integer>> getLevel(@PathVariable String username) {
        return ResponseEntity.ok(Map.of("level", playerService.getLevel(username)));
    }

    /** Gain experience. Auto-levels up if threshold is met. */
    @PostMapping("/{username}/experience")
    public ResponseEntity<Player> gainExperience(@PathVariable String username,
                                                  @RequestBody ExperienceRequest req) {
        return ResponseEntity.ok(playerService.gainExperience(username, req.getAmount()));
    }

    /** Manual level-up: resets XP, increases level and monster cap. */
    @PostMapping("/{username}/levelup")
    public ResponseEntity<Player> levelUp(@PathVariable String username) {
        return ResponseEntity.ok(playerService.levelUp(username));
    }

    /** Acquire a new monster (creates monster document and links it to the player). */
    @PostMapping("/{username}/monsters")
    public ResponseEntity<Player> addMonster(@PathVariable String username,
                                              @RequestBody CreateMonsterRequest req) {
        return ResponseEntity.ok(playerService.addMonster(username, req));
    }

    /** Rattache un monstre déjà existant au joueur (appelé par invocationapi). */
    @PostMapping("/{username}/monsters/link")
    public ResponseEntity<Player> linkMonster(@PathVariable String username,
                                               @RequestBody LinkMonsterRequest req) {
        return ResponseEntity.ok(playerService.linkMonster(username, req.getMonsterId()));
    }

    /** Remove a monster from the player's list and delete it. */
    @DeleteMapping("/{username}/monsters/{monsterId}")
    public ResponseEntity<Player> removeMonster(@PathVariable String username,
                                                 @PathVariable String monsterId) {
        return ResponseEntity.ok(playerService.removeMonster(username, monsterId));
    }
}
