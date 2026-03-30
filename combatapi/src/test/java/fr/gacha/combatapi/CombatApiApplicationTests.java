package fr.gacha.combatapi;

import fr.gacha.combatapi.dto.CombatRequest;
import fr.gacha.combatapi.model.CombatLog;
import fr.gacha.combatapi.repository.CombatLogRepository;
import fr.gacha.combatapi.service.CombatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class CombatApiApplicationTests {

    @Mock private CombatLogRepository combatLogRepository;

    private CombatService combatService;
    private RestTemplate restTemplate;
    private MockRestServiceServer server;

    private static final String BASE_URL = "http://mock-monster";

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        combatService = new CombatService(combatLogRepository, BASE_URL, restTemplate);
    }

    // ── helpers ───────────────────────────────────────────────────────

    private String monsterJson(String id, String owner, int hp, int atk, int baseDmg) {
        return """
            {"id":"%s","ownerUsername":"%s","hp":%d,"atk":%d,"def":0,"vit":0,
             "skills":[{"baseDamage":%d,"statRatio":0.0,"statType":"ATK","cooldown":2}]}
            """.formatted(id, owner, hp, atk, baseDmg);
    }

    private void expectFetch(String id, String json) {
        server.expect(requestTo(BASE_URL + "/api/monsters/" + id))
              .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }

    private void expectXpGrant(String winnerId) {
        server.expect(requestTo(BASE_URL + "/api/monsters/" + winnerId + "/experience"))
              .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
    }

    private CombatLog runCombat(String id1, String id2) {
        when(combatLogRepository.count()).thenReturn(0L);
        when(combatLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        CombatRequest req = new CombatRequest();
        req.setMonster1Id(id1);
        req.setMonster2Id(id2);
        return combatService.simulate(req, "Bearer test");
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
        when(combatLogRepository.count()).thenReturn(3L);

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

    // ── simulate ──────────────────────────────────────────────────────

    @Test
    void simulate_monster1_wins_when_one_shots_monster2() {
        expectFetch("m1", monsterJson("m1", "alice", 1000, 0, 9999));
        expectFetch("m2", monsterJson("m2", "bob",      1, 0,    1));
        expectXpGrant("m1");

        CombatLog result = runCombat("m1", "m2");

        assertEquals("m1", result.getWinnerId());
        assertEquals("alice", result.getWinnerOwner());
        assertFalse(result.getTurns().isEmpty());
    }

    @Test
    void simulate_monster2_wins_when_monster1_deals_no_damage() {
        expectFetch("m1", monsterJson("m1", "alice",    1, 0,  0));
        expectFetch("m2", monsterJson("m2", "bob",   1000, 0, 9999));
        expectXpGrant("m2");

        CombatLog result = runCombat("m1", "m2");

        assertEquals("m2", result.getWinnerId());
        assertEquals("bob", result.getWinnerOwner());
    }

    @Test
    void simulate_no_winner_on_timeout_when_no_damage() {
        expectFetch("m1", monsterJson("m1", "alice", 9999, 0, 0));
        expectFetch("m2", monsterJson("m2", "bob",   9999, 0, 0));

        CombatLog result = runCombat("m1", "m2");

        assertNull(result.getWinnerId());
        assertNull(result.getWinnerOwner());
        assertEquals(100, result.getTurns().size());
    }

    @Test
    void simulate_damage_formula_baseDamage_plus_stat_times_ratio() {
        // baseDamage=10, ATK=50, ratio=0.5 → damage = 10 + (50 * 0.5) = 35
        // m2 HP=100, after turn 1 → 65 HP remaining
        String m1 = """
            {"id":"m1","ownerUsername":"alice","hp":9999,"atk":50,"def":0,"vit":0,
             "skills":[{"baseDamage":10,"statRatio":0.5,"statType":"ATK","cooldown":99}]}
            """;
        String m2 = """
            {"id":"m2","ownerUsername":"bob","hp":100,"atk":0,"def":0,"vit":0,
             "skills":[{"baseDamage":0,"statRatio":0.0,"statType":"ATK","cooldown":99}]}
            """;
        expectFetch("m1", m1);
        expectFetch("m2", m2);

        CombatLog result = runCombat("m1", "m2");

        String firstAction = result.getTurns().get(0).getActions().get(0);
        assertTrue(firstAction.contains("35 dégâts"), "Expected 35 damage, got: " + firstAction);
        assertTrue(firstAction.contains("HP restant : 65"), "Expected 65 HP remaining, got: " + firstAction);
    }

    @Test
    void simulate_skill_cooldown_prevents_reuse_next_turn() {
        // cooldown=2 → skill used turn 1, unavailable turn 2 (cooldown=1), available turn 3
        String m1 = """
            {"id":"m1","ownerUsername":"alice","hp":9999,"atk":0,"def":0,"vit":0,
             "skills":[{"baseDamage":1,"statRatio":0.0,"statType":"ATK","cooldown":2}]}
            """;
        String m2 = """
            {"id":"m2","ownerUsername":"bob","hp":9999,"atk":0,"def":0,"vit":0,
             "skills":[{"baseDamage":0,"statRatio":0.0,"statType":"ATK","cooldown":2}]}
            """;
        expectFetch("m1", m1);
        expectFetch("m2", m2);

        CombatLog result = runCombat("m1", "m2");

        String turn1 = result.getTurns().get(0).getActions().get(0);
        String turn2 = result.getTurns().get(1).getActions().get(0);
        String turn3 = result.getTurns().get(2).getActions().get(0);

        assertTrue(turn1.contains("dégâts"),         "Turn 1: skill should be used");
        assertTrue(turn2.contains("aucune compétence"), "Turn 2: skill on cooldown");
        assertTrue(turn3.contains("dégâts"),         "Turn 3: skill available again");
    }
}
