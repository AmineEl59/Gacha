package fr.gacha.invocationapi;

import fr.gacha.invocationapi.model.BaseMonster;
import fr.gacha.invocationapi.model.Invocation;
import fr.gacha.invocationapi.model.InvocationStatus;
import fr.gacha.invocationapi.repository.BaseMonsterRepository;
import fr.gacha.invocationapi.repository.InvocationRepository;
import fr.gacha.invocationapi.service.InvocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvocationApiApplicationTests {

    @Mock private BaseMonsterRepository baseMonsterRepository;
    @Mock private InvocationRepository invocationRepository;

    private InvocationService invocationService;

    private static final int ITERATIONS = 10_000;
    private static final double TOLERANCE = 0.05; // ±5%

    @BeforeEach
    void setUp() {
        invocationService = new InvocationService(
                baseMonsterRepository, invocationRepository,
                "http://mock-monster", "http://mock-player");
    }

    // ── helpers ───────────────────────────────────────────────────────

    private BaseMonster monster(String name, double rate) {
        BaseMonster m = new BaseMonster();
        m.setName(name);
        m.setElementType("FIRE");
        m.setHp(100); m.setAtk(50); m.setDef(30); m.setVit(20);
        m.setInvocationRate(rate);
        return m;
    }

    // ── pickRandomMonster ─────────────────────────────────────────────

    @Test
    void pickRandomMonster_empty_list_throws() {
        when(baseMonsterRepository.findAll()).thenReturn(List.of());

        assertThrows(ResponseStatusException.class,
                () -> invocationService.pickRandomMonster());
    }

    @Test
    void pickRandomMonster_single_monster_always_returns_it() {
        BaseMonster only = monster("Solo", 100.0);
        when(baseMonsterRepository.findAll()).thenReturn(List.of(only));

        for (int i = 0; i < 100; i++) {
            assertEquals("Solo", invocationService.pickRandomMonster().getName());
        }
    }

    /**
     * Test statistique — vérifie que la distribution sur 10 000 tirages
     * respecte les taux définis (±5%).
     */
    @Test
    void pickRandomMonster_distribution_matches_rates() {
        List<BaseMonster> monsters = List.of(
                monster("Ignis",   30.0),
                monster("Zephyr",  30.0),
                monster("Aqua",    30.0),
                monster("Tsunami", 10.0)
        );
        when(baseMonsterRepository.findAll()).thenReturn(monsters);

        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < ITERATIONS; i++) {
            String name = invocationService.pickRandomMonster().getName();
            counts.merge(name, 1, Integer::sum);
        }

        assertRate(counts, "Ignis",   30.0);
        assertRate(counts, "Zephyr",  30.0);
        assertRate(counts, "Aqua",    30.0);
        assertRate(counts, "Tsunami", 10.0);
    }

    @Test
    void pickRandomMonster_rates_sum_less_than_100_returns_last() {
        List<BaseMonster> monsters = List.of(
                monster("A", 0.0001),
                monster("B", 0.0001)
        );
        when(baseMonsterRepository.findAll()).thenReturn(monsters);

        long countB = 0;
        for (int i = 0; i < 1000; i++) {
            if ("B".equals(invocationService.pickRandomMonster().getName())) countB++;
        }
        assertTrue(countB > 900, "Last monster should be the fallback when rates are near 0");
    }

    // ── getByUsername ─────────────────────────────────────────────────

    @Test
    void getByUsername_returns_only_invocations_of_that_user() {
        Invocation inv1 = new Invocation(); inv1.setUsername("alice");
        Invocation inv2 = new Invocation(); inv2.setUsername("alice");

        when(invocationRepository.findByUsername("alice")).thenReturn(List.of(inv1, inv2));

        List<Invocation> result = invocationService.getByUsername("alice");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(i -> "alice".equals(i.getUsername())));
    }

    @Test
    void getByUsername_returns_empty_list_when_no_invocations() {
        when(invocationRepository.findByUsername("bob")).thenReturn(List.of());

        List<Invocation> result = invocationService.getByUsername("bob");

        assertTrue(result.isEmpty());
    }

    // ── replayAll ─────────────────────────────────────────────────────

    @Test
    void replayAll_returns_still_pending_after_failed_calls() {
        Invocation pending = new Invocation();
        pending.setStatus(InvocationStatus.PENDING);
        pending.setUsername("alice");

        when(invocationRepository.findByStatusNot(InvocationStatus.COMPLETED))
                .thenReturn(List.of(pending));

        invocationService.replayAll("Bearer token");

        verify(invocationRepository, atLeastOnce()).findByStatusNot(InvocationStatus.COMPLETED);
    }

    // ── helper ────────────────────────────────────────────────────────

    private void assertRate(Map<String, Integer> counts, String name, double expectedPct) {
        int count = counts.getOrDefault(name, 0);
        double actualPct = (count * 100.0) / ITERATIONS;
        assertEquals(expectedPct, actualPct, TOLERANCE * 100,
                "Taux de " + name + " : attendu " + expectedPct + "%, obtenu " + actualPct + "%");
    }
}
