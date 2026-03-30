package fr.gacha.invocationapi.controller;

import fr.gacha.invocationapi.model.BaseMonster;
import fr.gacha.invocationapi.model.Invocation;
import fr.gacha.invocationapi.repository.BaseMonsterRepository;
import fr.gacha.invocationapi.service.InvocationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invocations")
public class InvocationController {

    private final InvocationService invocationService;
    private final BaseMonsterRepository baseMonsterRepository;

    public InvocationController(InvocationService invocationService,
                                 BaseMonsterRepository baseMonsterRepository) {
        this.invocationService = invocationService;
        this.baseMonsterRepository = baseMonsterRepository;
    }

    /** Invoquer un monstre aléatoire pour le joueur authentifié. */
    @PostMapping
    public ResponseEntity<Invocation> invoke(Authentication auth, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return ResponseEntity.ok(invocationService.invoke(auth.getName(), authHeader));
    }

    /** Rejouer toutes les invocations non terminées. */
    @PostMapping("/replay")
    public ResponseEntity<List<Invocation>> replay(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return ResponseEntity.ok(invocationService.replayAll(authHeader));
    }

    /** Récupérer l'historique des invocations du joueur authentifié. */
    @GetMapping
    public ResponseEntity<List<Invocation>> getAll(Authentication auth) {
        return ResponseEntity.ok(invocationService.getByUsername(auth.getName()));
    }

    /** Récupérer tous les monstres invocables (avec leurs taux). */
    @GetMapping("/monsters")
    public ResponseEntity<List<BaseMonster>> getBaseMonsters() {
        return ResponseEntity.ok(baseMonsterRepository.findAll());
    }
}
