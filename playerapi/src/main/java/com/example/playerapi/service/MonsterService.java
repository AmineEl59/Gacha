package com.example.playerapi.service;

import com.example.playerapi.model.Monster;
import com.example.playerapi.model.Skill;
import com.example.playerapi.repository.MonsterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MonsterService {

    private final MonsterRepository monsterRepository;

    public MonsterService(MonsterRepository monsterRepository) {
        this.monsterRepository = monsterRepository;
    }

    // ── Threshold formula (same as player) ──────────────────────────
    private int computeThreshold(int level) {
        if (level <= 1) return 50;
        return (int) Math.round(50 * Math.pow(1.1, level - 1));
    }

    // ── Queries ───────────────────────────────────────────────────────

    public Monster getMonster(String monsterId, String requesterUsername) {
        Monster monster = monsterRepository.findById(monsterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monster not found"));
        if (!monster.getOwnerUsername().equals(requesterUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return monster;
    }

    // ── Experience ────────────────────────────────────────────────────

    /** Give XP to a monster; cascade level-ups. Each level gained: stats +5, +1 skillPoint. */
    public Monster gainExperience(String monsterId, String requesterUsername, int amount) {
        Monster monster = getMonster(monsterId, requesterUsername);
        monster.setExperience(monster.getExperience() + amount);

        while (monster.getExperience() >= monster.getExperienceThreshold()) {
            monster.setExperience(monster.getExperience() - monster.getExperienceThreshold());
            doLevelUp(monster);
        }

        return monsterRepository.save(monster);
    }

    private void doLevelUp(Monster monster) {
        monster.setLevel(monster.getLevel() + 1);
        monster.setExperienceThreshold(computeThreshold(monster.getLevel()));
        monster.setSkillPoints(monster.getSkillPoints() + 1);
        // Stat increase per level
        monster.setHp(monster.getHp() + 5);
        monster.setAtk(monster.getAtk() + 5);
        monster.setDef(monster.getDef() + 5);
        monster.setVit(monster.getVit() + 5);
    }

    // ── Skill improvement ─────────────────────────────────────────────

    /** Spend one skill point to improve a skill at the given index. */
    public Monster improveSkill(String monsterId, String requesterUsername, int skillIndex) {
        Monster monster = getMonster(monsterId, requesterUsername);

        if (monster.getSkillPoints() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No skill points available");
        }
        if (skillIndex < 0 || skillIndex >= monster.getSkills().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid skill index");
        }

        Skill skill = monster.getSkills().get(skillIndex);
        if (skill.getImprovementLevel() >= skill.getMaxImprovementLevel()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Skill is already at max improvement level");
        }

        skill.setImprovementLevel(skill.getImprovementLevel() + 1);
        // Each improvement boosts base damage by 2
        skill.setBaseDamage(skill.getBaseDamage() + 2);
        monster.setSkillPoints(monster.getSkillPoints() - 1);

        return monsterRepository.save(monster);
    }
}
