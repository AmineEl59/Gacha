package fr.gacha.monsterapi.service;

import fr.gacha.monsterapi.dto.CreateMonsterRequest;
import fr.gacha.monsterapi.model.Monster;
import fr.gacha.monsterapi.model.Skill;
import fr.gacha.monsterapi.repository.MonsterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MonsterService {

    private final MonsterRepository monsterRepository;

    public MonsterService(MonsterRepository monsterRepository) {
        this.monsterRepository = monsterRepository;
    }

    private int computeThreshold(int level) {
        if (level <= 1) return 50;
        return (int) Math.round(50 * Math.pow(1.1, level - 1));
    }

    // ── CRUD ──────────────────────────────────────────────────────────

    public Monster createMonster(String ownerUsername, CreateMonsterRequest req) {
        Monster monster = new Monster(ownerUsername, req.getElementType(),
                req.getHp(), req.getAtk(), req.getDef(), req.getVit());
        return monsterRepository.save(monster);
    }

    /** Récupère un monstre sans vérification d'ownership (lecture publique). */
    public Monster getMonsterById(String monsterId) {
        return monsterRepository.findById(monsterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monster not found"));
    }

    /** Récupère un monstre en vérifiant que le demandeur en est le propriétaire. */
    public Monster getMonster(String monsterId, String requesterUsername) {
        Monster monster = getMonsterById(monsterId);
        if (!monster.getOwnerUsername().equals(requesterUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return monster;
    }

    public void deleteMonster(String monsterId, String requesterUsername) {
        Monster monster = monsterRepository.findById(monsterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monster not found"));
        if (!monster.getOwnerUsername().equals(requesterUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        monsterRepository.deleteById(monsterId);
    }

    // ── Experience ────────────────────────────────────────────────────

    public Monster gainExperience(String monsterId, String requesterUsername, int amount) {
        Monster monster = getMonsterById(monsterId);
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
        monster.setHp(monster.getHp() + 5);
        monster.setAtk(monster.getAtk() + 5);
        monster.setDef(monster.getDef() + 5);
        monster.setVit(monster.getVit() + 5);
    }

    // ── Skill improvement ─────────────────────────────────────────────

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
        skill.setBaseDamage(skill.getBaseDamage() + 2);
        monster.setSkillPoints(monster.getSkillPoints() - 1);

        return monsterRepository.save(monster);
    }
}
