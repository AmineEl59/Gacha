package com.example.playerapi.controller;

import com.example.playerapi.dto.ExperienceRequest;
import com.example.playerapi.model.Monster;
import com.example.playerapi.service.MonsterService;
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

    /** Get monster details (only accessible to the owning player). */
    @GetMapping("/{id}")
    public ResponseEntity<Monster> getMonster(@PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(monsterService.getMonster(id, auth.getName()));
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
