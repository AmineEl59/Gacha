package com.example.combatapi.service;

import com.example.combatapi.dto.CombatRequest;
import com.example.combatapi.model.CombatLog;
import com.example.combatapi.model.TurnLog;
import com.example.combatapi.repository.CombatLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CombatService {

    private static final int MAX_TURNS = 100;

    private final CombatLogRepository combatLogRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String monsterApiUrl;

    public CombatService(CombatLogRepository combatLogRepository,
                         @Value("${monster.api.url}") String monsterApiUrl) {
        this.combatLogRepository = combatLogRepository;
        this.monsterApiUrl = monsterApiUrl;
    }

    // ── État interne d'un monstre pendant le combat ───────────────────

    private static class CombatMonster {
        String id;
        String ownerUsername;
        int currentHp;
        int atk, def, vit;
        List<CombatSkill> skills = new ArrayList<>();

        int getStat(String statType) {
            return switch (statType.toUpperCase()) {
                case "ATK" -> atk;
                case "DEF" -> def;
                case "VIT" -> vit;
                case "HP"  -> currentHp;
                default    -> 0;
            };
        }

        /** Retourne le skill avec le plus grand index dont le cooldown est à 0. */
        CombatSkill getBestAvailableSkill() {
            for (int i = skills.size() - 1; i >= 0; i--) {
                if (skills.get(i).currentCooldown == 0) return skills.get(i);
            }
            return null;
        }

        void decrementCooldowns() {
            for (CombatSkill skill : skills) {
                if (skill.currentCooldown > 0) skill.currentCooldown--;
            }
        }
    }

    private static class CombatSkill {
        int index;
        int baseDamage;
        double statRatio;
        String statType;
        int baseCooldown;
        int currentCooldown; // 0 au départ = disponible immédiatement
    }

    // ── Combat ────────────────────────────────────────────────────────

    public CombatLog simulate(CombatRequest req, String authHeader) {
        CombatMonster m1 = fetchMonster(req.getMonster1Id(), authHeader);
        CombatMonster m2 = fetchMonster(req.getMonster2Id(), authHeader);

        List<TurnLog> turns = new ArrayList<>();
        int turn = 1;

        while (m1.currentHp > 0 && m2.currentHp > 0 && turn <= MAX_TURNS) {
            TurnLog turnLog = new TurnLog(turn);

            // Décrémenter les cooldowns en début de tour
            m1.decrementCooldowns();
            m2.decrementCooldowns();

            // Monster 1 attaque
            CombatSkill skill1 = m1.getBestAvailableSkill();
            if (skill1 != null) {
                int damage = skill1.baseDamage + (int)(m1.getStat(skill1.statType) * skill1.statRatio);
                m2.currentHp -= damage;
                skill1.currentCooldown = skill1.baseCooldown;
                turnLog.addAction("Monstre " + m1.id + " utilise compétence " + (skill1.index + 1)
                        + " → " + damage + " dégâts sur monstre " + m2.id
                        + " (HP restant : " + Math.max(0, m2.currentHp) + ")");
            } else {
                turnLog.addAction("Monstre " + m1.id + " : aucune compétence disponible");
            }

            // Monster 2 attaque (seulement s'il est encore en vie)
            if (m2.currentHp > 0) {
                CombatSkill skill2 = m2.getBestAvailableSkill();
                if (skill2 != null) {
                    int damage = skill2.baseDamage + (int)(m2.getStat(skill2.statType) * skill2.statRatio);
                    m1.currentHp -= damage;
                    skill2.currentCooldown = skill2.baseCooldown;
                    turnLog.addAction("Monstre " + m2.id + " utilise compétence " + (skill2.index + 1)
                            + " → " + damage + " dégâts sur monstre " + m1.id
                            + " (HP restant : " + Math.max(0, m1.currentHp) + ")");
                } else {
                    turnLog.addAction("Monstre " + m2.id + " : aucune compétence disponible");
                }
            }

            turns.add(turnLog);
            turn++;
        }

        // Déterminer le vainqueur
        CombatLog log = new CombatLog();
        log.setCombatNumber((int) combatLogRepository.count() + 1);
        log.setMonster1Id(req.getMonster1Id());
        log.setMonster2Id(req.getMonster2Id());
        log.setTurns(turns);

        if (m1.currentHp > 0 && m2.currentHp <= 0) {
            log.setWinnerId(m1.id);
            log.setWinnerOwner(m1.ownerUsername);
        } else if (m2.currentHp > 0 && m1.currentHp <= 0) {
            log.setWinnerId(m2.id);
            log.setWinnerOwner(m2.ownerUsername);
        }
        // Si les deux sont morts ou timeout : pas de vainqueur (winnerId reste null)

        return combatLogRepository.save(log);
    }

    // ── Historique & replay ───────────────────────────────────────────

    public List<CombatLog> getAll() {
        return combatLogRepository.findAll();
    }

    public CombatLog getByCombatNumber(int combatNumber) {
        return combatLogRepository.findByCombatNumber(combatNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Combat " + combatNumber + " introuvable"));
    }

    // ── Récupération du monstre depuis monsterapi ─────────────────────

    @SuppressWarnings("unchecked")
    private CombatMonster fetchMonster(String monsterId, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        Map<String, Object> data = restTemplate.exchange(
                monsterApiUrl + "/api/monsters/" + monsterId,
                HttpMethod.GET,
                entity,
                Map.class
        ).getBody();

        if (data == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Monstre " + monsterId + " introuvable");
        }

        CombatMonster monster = new CombatMonster();
        monster.id            = monsterId;
        monster.ownerUsername = (String) data.get("ownerUsername");
        monster.currentHp     = (int) data.get("hp");
        monster.atk           = (int) data.get("atk");
        monster.def           = (int) data.get("def");
        monster.vit           = (int) data.get("vit");

        List<Map<String, Object>> rawSkills = (List<Map<String, Object>>) data.get("skills");
        if (rawSkills != null) {
            for (int i = 0; i < rawSkills.size(); i++) {
                Map<String, Object> s = rawSkills.get(i);
                CombatSkill skill = new CombatSkill();
                skill.index        = i;
                skill.baseDamage   = (int) s.get("baseDamage");
                skill.statRatio    = ((Number) s.get("statRatio")).doubleValue();
                skill.statType     = (String) s.get("statType");
                skill.baseCooldown = (int) s.get("cooldown");
                skill.currentCooldown = 0; // disponible dès le départ
                monster.skills.add(skill);
            }
        }

        return monster;
    }
}
