package com.example.playerapi.service;

import com.example.playerapi.dto.CreateMonsterRequest;
import com.example.playerapi.model.Monster;
import com.example.playerapi.model.Player;
import com.example.playerapi.repository.MonsterRepository;
import com.example.playerapi.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final MonsterRepository monsterRepository;

    public PlayerService(PlayerRepository playerRepository, MonsterRepository monsterRepository) {
        this.playerRepository = playerRepository;
        this.monsterRepository = monsterRepository;
    }

    // ── Threshold formula ───────────────────────────────────────────
    // Level 1 → 2 : 50 XP
    // Level 2 → 3 : 55 XP  (50 * 1.1^1)
    // Level n → n+1 : round(50 * 1.1^(n-1))
    private int computeThreshold(int level) {
        if (level <= 1) return 50;
        return (int) Math.round(50 * Math.pow(1.1, level - 1));
    }

    // ── CRUD ──────────────────────────────────────────────────────────

    public Player createPlayer(String username) {
        if (playerRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Player already exists");
        }
        return playerRepository.save(new Player(username));
    }

    public Player getProfile(String username) {
        return playerRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
    }

    public List<String> getMonsters(String username) {
        return getProfile(username).getMonsters();
    }

    public int getLevel(String username) {
        return getProfile(username).getLevel();
    }

    // ── Experience ────────────────────────────────────────────────────

    /** Add XP and cascade level-ups automatically. */
    public Player gainExperience(String username, int amount) {
        Player player = getProfile(username);
        player.setExperience(player.getExperience() + amount);

        while (player.getLevel() < 50 && player.getExperience() >= player.getExperienceThreshold()) {
            player.setExperience(player.getExperience() - player.getExperienceThreshold());
            doLevelUp(player);
        }

        return playerRepository.save(player);
    }

    /** Manual level-up: reset XP, raise level, recalculate threshold and max monsters. */
    public Player levelUp(String username) {
        Player player = getProfile(username);
        if (player.getLevel() >= 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player is already at max level");
        }
        player.setExperience(0);
        doLevelUp(player);
        return playerRepository.save(player);
    }

    private void doLevelUp(Player player) {
        player.setLevel(player.getLevel() + 1);
        player.setExperienceThreshold(computeThreshold(player.getLevel()));
        player.setMaxMonsters(10 + player.getLevel());
    }

    // ── Monster management ────────────────────────────────────────────

    public Player addMonster(String username, CreateMonsterRequest req) {
        Player player = getProfile(username);
        if (player.getMonsters().size() >= player.getMaxMonsters()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Monster list is full");
        }
        Monster monster = new Monster(username, req.getElementType(),
                req.getHp(), req.getAtk(), req.getDef(), req.getVit());
        Monster saved = monsterRepository.save(monster);
        player.getMonsters().add(saved.getId());
        return playerRepository.save(player);
    }

    public Player removeMonster(String username, String monsterId) {
        Player player = getProfile(username);
        if (!player.getMonsters().remove(monsterId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Monster not found in player list");
        }
        monsterRepository.deleteById(monsterId);
        return playerRepository.save(player);
    }
}
