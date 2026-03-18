package com.example.monsterapi.controller;

import com.example.monsterapi.dto.CreateMonsterRequest;
import com.example.monsterapi.dto.ExperienceRequest;
import com.example.monsterapi.model.Monster;
import com.example.monsterapi.service.MonsterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monsters")
public class MonsterController {

    private final MonsterService monsterService;

    public MonsterController(MonsterService monsterService) {
        this.monsterService = monsterService;
    }

    /** Create a new monster; the owner is the authenticated user. */
    @PostMapping
    public ResponseEntity<Monster> createMonster(@RequestBody CreateMonsterRequest req,
                                                  Authentication auth) {
        return ResponseEntity.ok(monsterService.createMonster(auth.getName(), req));
    }

    /** Get monster details (accessible à tout joueur authentifié). */
    @GetMapping("/{id}")
    public ResponseEntity<Monster> getMonster(@PathVariable String id) {
        return ResponseEntity.ok(monsterService.getMonsterById(id));
    }

    /** Delete a monster (only accessible to the owning player). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonster(@PathVariable String id, Authentication auth) {
        monsterService.deleteMonster(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    /** Give XP to a monster; auto-levels up if threshold is met. */
    @PostMapping("/{id}/experience")
    public ResponseEntity<Monster> gainExperience(@PathVariable String id,
                                                   @RequestBody ExperienceRequest req,
                                                   Authentication auth) {
        return ResponseEntity.ok(monsterService.gainExperience(id, auth.getName(), req.getAmount()));
    }

    /** Spend one skill point to improve a skill (0-indexed). */
    @PostMapping("/{id}/skills/{skillIndex}/improve")
    public ResponseEntity<Monster> improveSkill(@PathVariable String id,
                                                 @PathVariable int skillIndex,
                                                 Authentication auth) {
        return ResponseEntity.ok(monsterService.improveSkill(id, auth.getName(), skillIndex));
    }
}
