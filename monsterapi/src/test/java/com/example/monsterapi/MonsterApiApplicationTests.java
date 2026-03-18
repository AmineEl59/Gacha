package com.example.monsterapi;

import com.example.monsterapi.dto.CreateMonsterRequest;
import com.example.monsterapi.model.ElementType;
import com.example.monsterapi.model.Monster;
import com.example.monsterapi.repository.MonsterRepository;
import com.example.monsterapi.service.MonsterService;
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
class MonsterApiApplicationTests {

    @Mock private MonsterRepository monsterRepository;

    private MonsterService monsterService;

    @BeforeEach
    void setUp() {
        monsterService = new MonsterService(monsterRepository);
    }

    private Monster buildMonster(String id, String owner) {
        Monster m = new Monster(owner, ElementType.FIRE, 100, 50, 30, 20);
        m.setId(id);
        return m;
    }

    // ── createMonster ─────────────────────────────────────────────────

    @Test
    void createMonster_has_three_default_skills() {
        CreateMonsterRequest req = new CreateMonsterRequest();
        req.setElementType(ElementType.FIRE);
        req.setHp(100); req.setAtk(50); req.setDef(30); req.setVit(20);
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Monster result = monsterService.createMonster("alice", req);

        assertEquals(3, result.getSkills().size());
        assertEquals("alice", result.getOwnerUsername());
        assertEquals(ElementType.FIRE, result.getElementType());
    }

    @Test
    void createMonster_starts_at_level_1_with_no_xp() {
        CreateMonsterRequest req = new CreateMonsterRequest();
        req.setElementType(ElementType.WATER);
        req.setHp(200); req.setAtk(40); req.setDef(60); req.setVit(30);
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Monster result = monsterService.createMonster("bob", req);

        assertEquals(1, result.getLevel());
        assertEquals(0, result.getExperience());
        assertEquals(0, result.getSkillPoints());
    }

    // ── getMonster ────────────────────────────────────────────────────

    @Test
    void getMonsterById_found_returns_monster() {
        Monster monster = buildMonster("id1", "alice");
        when(monsterRepository.findById("id1")).thenReturn(Optional.of(monster));

        Monster result = monsterService.getMonsterById("id1");

        assertEquals("id1", result.getId());
    }

    @Test
    void getMonsterById_not_found_throws_404() {
        when(monsterRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> monsterService.getMonsterById("unknown"));
    }

    @Test
    void getMonster_wrong_owner_throws_403() {
        Monster monster = buildMonster("id1", "alice");
        when(monsterRepository.findById("id1")).thenReturn(Optional.of(monster));

        assertThrows(ResponseStatusException.class,
                () -> monsterService.getMonster("id1", "bob"));
    }

    // ── gainExperience ────────────────────────────────────────────────

    @Test
    void gainExperience_below_threshold_no_levelup() {
        Monster monster = buildMonster("id1", "alice");
        when(monsterRepository.findById("id1")).thenReturn(Optional.of(monster));
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Monster result = monsterService.gainExperience("id1", "alice", 30);

        assertEquals(30, result.getExperience());
        assertEquals(1, result.getLevel());
    }

    @Test
    void gainExperience_levelup_increases_stats_and_grants_skill_point() {
        Monster monster = buildMonster("id1", "alice");
        int initialHp = monster.getHp();
        when(monsterRepository.findById("id1")).thenReturn(Optional.of(monster));
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Monster result = monsterService.gainExperience("id1", "alice", 50);

        assertEquals(2, result.getLevel());
        assertEquals(initialHp + 5, result.getHp());
        assertEquals(1, result.getSkillPoints());
    }

    @Test
    void gainExperience_multiple_levelups_stacks_stats() {
        Monster monster = buildMonster("id1", "alice");
        int initialAtk = monster.getAtk();
        when(monsterRepository.findById("id1")).thenReturn(Optional.of(monster));
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // 50 + 55 = 105 XP → level up twice
        Monster result = monsterService.gainExperience("id1", "alice", 105);

        assertEquals(3, result.getLevel());
        assertEquals(initialAtk + 10, result.getAtk());
        assertEquals(2, result.getSkillPoints());
    }

    // ── improveSkill ──────────────────────────────────────────────────

    @Test
    void improveSkill_success_spends_skill_point() {
        Monster monster = buildMonster("id1", "alice");
        monster.setSkillPoints(1);
        int initialDamage = monster.getSkills().get(0).getBaseDamage();
        when(monsterRepository.findById("id1")).thenReturn(Optional.of(monster));
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Monster result = monsterService.improveSkill("id1", "alice", 0);

        assertEquals(0, result.getSkillPoints());
        assertEquals(initialDamage + 2, result.getSkills().get(0).getBaseDamage());
        assertEquals(1, result.getSkills().get(0).getImprovementLevel());
    }

    @Test
    void improveSkill_no_skill_points_throws() {
        Monster monster = buildMonster("id1", "alice");
        monster.setSkillPoints(0);
        when(monsterRepository.findById("id1")).thenReturn(Optional.of(monster));

        assertThrows(ResponseStatusException.class,
                () -> monsterService.improveSkill("id1", "alice", 0));
    }

    @Test
    void improveSkill_invalid_index_throws() {
        Monster monster = buildMonster("id1", "alice");
        monster.setSkillPoints(1);
        when(monsterRepository.findById("id1")).thenReturn(Optional.of(monster));

        assertThrows(ResponseStatusException.class,
                () -> monsterService.improveSkill("id1", "alice", 5));
    }

    @Test
    void improveSkill_at_max_level_throws() {
        Monster monster = buildMonster("id1", "alice");
        monster.setSkillPoints(1);
        monster.getSkills().get(0).setImprovementLevel(5);
        monster.getSkills().get(0).setMaxImprovementLevel(5);
        when(monsterRepository.findById("id1")).thenReturn(Optional.of(monster));

        assertThrows(ResponseStatusException.class,
                () -> monsterService.improveSkill("id1", "alice", 0));
    }
}
