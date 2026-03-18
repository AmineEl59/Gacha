package com.example.playerapi;

import com.example.playerapi.model.Player;
import com.example.playerapi.repository.PlayerRepository;
import com.example.playerapi.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerapiApplicationTests {

    @Mock private PlayerRepository playerRepository;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService(playerRepository, "http://mock-monster-api");
    }

    // ── createPlayer ──────────────────────────────────────────────────

    @Test
    void createPlayer_success_sets_defaults() {
        when(playerRepository.existsByUsername("alice")).thenReturn(false);
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        Player player = playerService.createPlayer("alice");

        assertEquals("alice", player.getUsername());
        assertEquals(0, player.getLevel());
        assertEquals(0, player.getExperience());
        assertEquals(50, player.getExperienceThreshold());
        assertEquals(10, player.getMaxMonsters());
    }

    @Test
    void createPlayer_duplicate_throws_conflict() {
        when(playerRepository.existsByUsername("alice")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> playerService.createPlayer("alice"));
        verify(playerRepository, never()).save(any());
    }

    // ── gainExperience ────────────────────────────────────────────────

    @Test
    void gainExperience_below_threshold_no_levelup() {
        Player player = new Player("alice");
        when(playerRepository.findByUsername("alice")).thenReturn(Optional.of(player));
        when(playerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Player result = playerService.gainExperience("alice", 30);

        assertEquals(30, result.getExperience());
        assertEquals(0, result.getLevel());
    }

    @Test
    void gainExperience_reaches_threshold_triggers_levelup() {
        Player player = new Player("alice");
        when(playerRepository.findByUsername("alice")).thenReturn(Optional.of(player));
        when(playerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Player result = playerService.gainExperience("alice", 50);

        assertEquals(1, result.getLevel());
        assertEquals(0, result.getExperience());
    }

    @Test
    void gainExperience_multiple_levelups_in_one_call() {
        Player player = new Player("alice");
        when(playerRepository.findByUsername("alice")).thenReturn(Optional.of(player));
        when(playerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // 50 + 55 = 105 XP → should level up twice
        Player result = playerService.gainExperience("alice", 105);

        assertEquals(2, result.getLevel());
    }

    @Test
    void gainExperience_stops_at_max_level_50() {
        Player player = new Player("alice");
        player.setLevel(50);
        player.setExperienceThreshold(50);
        when(playerRepository.findByUsername("alice")).thenReturn(Optional.of(player));
        when(playerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Player result = playerService.gainExperience("alice", 1000);

        assertEquals(50, result.getLevel());
    }

    // ── levelUp ───────────────────────────────────────────────────────

    @Test
    void levelUp_resets_xp_and_increases_level() {
        Player player = new Player("alice");
        player.setLevel(1);
        player.setExperience(30);
        when(playerRepository.findByUsername("alice")).thenReturn(Optional.of(player));
        when(playerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Player result = playerService.levelUp("alice");

        assertEquals(2, result.getLevel());
        assertEquals(0, result.getExperience());
        assertEquals(11, result.getMaxMonsters());
    }

    @Test
    void levelUp_at_max_level_throws() {
        Player player = new Player("alice");
        player.setLevel(50);
        when(playerRepository.findByUsername("alice")).thenReturn(Optional.of(player));

        assertThrows(ResponseStatusException.class, () -> playerService.levelUp("alice"));
    }

    // ── linkMonster ───────────────────────────────────────────────────

    @Test
    void linkMonster_adds_id_to_player() {
        Player player = new Player("alice");
        when(playerRepository.findByUsername("alice")).thenReturn(Optional.of(player));
        when(playerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Player result = playerService.linkMonster("alice", "monster-id-123");

        assertTrue(result.getMonsters().contains("monster-id-123"));
        assertEquals(1, result.getMonsters().size());
    }

    @Test
    void linkMonster_full_list_throws() {
        Player player = new Player("alice");
        player.setMaxMonsters(1);
        player.getMonsters().add("existing-monster");
        when(playerRepository.findByUsername("alice")).thenReturn(Optional.of(player));

        assertThrows(ResponseStatusException.class,
                () -> playerService.linkMonster("alice", "new-monster"));
    }

    // ── xp threshold formula ──────────────────────────────────────────

    @Test
    void threshold_formula_matches_spec() {
        // Spec: level 1→2 : 50 XP, level 2→3 : 55 XP (50 * 1.1)
        Player player = new Player("alice");
        when(playerRepository.findByUsername("alice")).thenReturn(Optional.of(player));
        when(playerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Player lvl1 = playerService.gainExperience("alice", 50);
        assertEquals(1, lvl1.getLevel());
        assertEquals(55, lvl1.getExperienceThreshold());
    }
}
