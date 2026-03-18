package com.example.invocationapi.controller;

import com.example.invocationapi.model.BaseMonster;
import com.example.invocationapi.model.Invocation;
import com.example.invocationapi.repository.BaseMonsterRepository;
import com.example.invocationapi.service.InvocationService;
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

    /** Récupérer l'historique de toutes les invocations. */
    @GetMapping
    public ResponseEntity<List<Invocation>> getAll() {
        return ResponseEntity.ok(invocationService.getAll());
    }

    /** Récupérer tous les monstres invocables (avec leurs taux). */
    @GetMapping("/monsters")
    public ResponseEntity<List<BaseMonster>> getBaseMonsters() {
        return ResponseEntity.ok(baseMonsterRepository.findAll());
    }
}
