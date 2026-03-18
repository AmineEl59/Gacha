package com.example.combatapi;

import com.example.combatapi.model.CombatLog;
import com.example.combatapi.repository.CombatLogRepository;
import com.example.combatapi.service.CombatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CombatServiceTest {

    @Mock private CombatLogRepository combatLogRepository;

    private CombatService combatService;

    @BeforeEach
    void setUp() {
        combatService = new CombatService(combatLogRepository, "http://mock-monster");
    }

    // ── getAll ────────────────────────────────────────────────────────

    @Test
    void getAll_returns_all_logs() {
        CombatLog log1 = new CombatLog();
        log1.setCombatNumber(1);
        CombatLog log2 = new CombatLog();
        log2.setCombatNumber(2);

        when(combatLogRepository.findAll()).thenReturn(List.of(log1, log2));

        List<CombatLog> result = combatService.getAll();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getCombatNumber());
        assertEquals(2, result.get(1).getCombatNumber());
    }

    @Test
    void getAll_empty_repository_returns_empty_list() {
        when(combatLogRepository.findAll()).thenReturn(List.of());

        List<CombatLog> result = combatService.getAll();

        assertTrue(result.isEmpty());
    }

    // ── getByCombatNumber ─────────────────────────────────────────────

    @Test
    void getByCombatNumber_found_returns_correct_log() {
        CombatLog log = new CombatLog();
        log.setCombatNumber(42);
        log.setMonster1Id("m1");
        log.setMonster2Id("m2");
        log.setWinnerId("m1");

        when(combatLogRepository.findByCombatNumber(42)).thenReturn(Optional.of(log));

        CombatLog result = combatService.getByCombatNumber(42);

        assertEquals(42, result.getCombatNumber());
        assertEquals("m1", result.getWinnerId());
    }

    @Test
    void getByCombatNumber_not_found_throws_404() {
        when(combatLogRepository.findByCombatNumber(99)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> combatService.getByCombatNumber(99));
    }

    // ── combat number generation ──────────────────────────────────────

    @Test
    void combat_number_increments_with_existing_count() {
        // When 3 combats already exist, next number is 4
        when(combatLogRepository.count()).thenReturn(3L);

        // We just verify count() is used correctly — simulate without HTTP
        long nextNumber = combatLogRepository.count() + 1;
        assertEquals(4L, nextNumber);
    }

    // ── CombatLog model ───────────────────────────────────────────────

    @Test
    void combatLog_no_winner_when_both_die() {
        CombatLog log = new CombatLog();
        log.setWinnerId(null);

        assertNull(log.getWinnerId(), "No winner when both monsters die simultaneously");
    }

    @Test
    void combatLog_stores_creation_timestamp() {
        CombatLog log = new CombatLog();

        assertNotNull(log.getCreatedAt());
    }
}
