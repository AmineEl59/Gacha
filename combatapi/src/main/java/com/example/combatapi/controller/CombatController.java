package com.example.combatapi.controller;

import com.example.combatapi.dto.CombatRequest;
import com.example.combatapi.model.CombatLog;
import com.example.combatapi.service.CombatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/combats")
public class CombatController {

    private final CombatService combatService;

    public CombatController(CombatService combatService) {
        this.combatService = combatService;
    }

    /** Lancer un combat entre 2 monstres. */
    @PostMapping
    public ResponseEntity<CombatLog> simulate(@RequestBody CombatRequest req,
                                               HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return ResponseEntity.ok(combatService.simulate(req, authHeader));
    }

    /** Historique de tous les combats. */
    @GetMapping
    public ResponseEntity<List<CombatLog>> getAll() {
        return ResponseEntity.ok(combatService.getAll());
    }

    /** Rediffusion d'un combat par son numéro. */
    @GetMapping("/{combatNumber}")
    public ResponseEntity<CombatLog> getByCombatNumber(@PathVariable int combatNumber) {
        return ResponseEntity.ok(combatService.getByCombatNumber(combatNumber));
    }
}
