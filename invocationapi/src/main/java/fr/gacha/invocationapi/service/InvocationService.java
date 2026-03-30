package fr.gacha.invocationapi.service;

import fr.gacha.invocationapi.model.BaseMonster;
import fr.gacha.invocationapi.model.Invocation;
import fr.gacha.invocationapi.model.InvocationStatus;
import fr.gacha.invocationapi.repository.BaseMonsterRepository;
import fr.gacha.invocationapi.repository.InvocationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InvocationService {

    private final BaseMonsterRepository baseMonsterRepository;
    private final InvocationRepository invocationRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private final String monsterApiUrl;
    private final String playerApiUrl;

    public InvocationService(BaseMonsterRepository baseMonsterRepository,
                              InvocationRepository invocationRepository,
                              @Value("${monster.api.url}") String monsterApiUrl,
                              @Value("${player.api.url}") String playerApiUrl) {
        this.baseMonsterRepository = baseMonsterRepository;
        this.invocationRepository = invocationRepository;
        this.monsterApiUrl = monsterApiUrl;
        this.playerApiUrl = playerApiUrl;
    }

    // ── Invocation ────────────────────────────────────────────────────

    public Invocation invoke(String username, String authHeader) {
        // 1. Tirer un monstre au sort selon les taux d'invocation
        BaseMonster picked = pickRandomMonster();

        // 2. Créer l'enregistrement dans la base tampon
        Invocation invocation = new Invocation();
        invocation.setUsername(username);
        invocation.setBaseMonsterName(picked.getName());
        invocation.setElementType(picked.getElementType());
        invocation.setHp(picked.getHp());
        invocation.setAtk(picked.getAtk());
        invocation.setDef(picked.getDef());
        invocation.setVit(picked.getVit());
        invocation = invocationRepository.save(invocation);

        // 3. Traiter chaque étape
        return processInvocation(invocation, authHeader);
    }

    // ── Replay ────────────────────────────────────────────────────────

    /** Rejoue toutes les invocations non terminées. */
    public List<Invocation> replayAll(String authHeader) {
        List<Invocation> pending = invocationRepository.findByStatusNot(InvocationStatus.COMPLETED);
        for (Invocation invocation : pending) {
            try {
                processInvocation(invocation, authHeader);
            } catch (Exception e) {
                // On continue même si une invocation échoue
            }
        }
        return invocationRepository.findByStatusNot(InvocationStatus.COMPLETED);
    }

    // ── Historique ────────────────────────────────────────────────────

    public List<Invocation> getByUsername(String username) {
        return invocationRepository.findByUsername(username);
    }

    // ── Logique interne ───────────────────────────────────────────────

    /**
     * Traite une invocation étape par étape.
     * Reprend là où elle s'était arrêtée grâce au status.
     */
    private Invocation processInvocation(Invocation invocation, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.set("Content-Type", "application/json");

        // Étape 1 : créer le monstre dans monsterapi
        if (invocation.getStatus() == InvocationStatus.PENDING) {
            Map<String, Object> body = new HashMap<>();
            body.put("elementType", invocation.getElementType());
            body.put("hp", invocation.getHp());
            body.put("atk", invocation.getAtk());
            body.put("def", invocation.getDef());
            body.put("vit", invocation.getVit());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            @SuppressWarnings("unchecked")
            Map<String, Object> monsterResponse = restTemplate.postForObject(
                    monsterApiUrl + "/api/monsters", entity, Map.class);

            if (monsterResponse == null || monsterResponse.get("id") == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Échec création monstre");
            }

            invocation.setMonsterId((String) monsterResponse.get("id"));
            invocation.setStatus(InvocationStatus.MONSTER_CREATED);
            invocation = invocationRepository.save(invocation);
        }

        // Étape 2 : rattacher le monstre au joueur dans playerapi
        if (invocation.getStatus() == InvocationStatus.MONSTER_CREATED) {
            Map<String, String> body = new HashMap<>();
            body.put("monsterId", invocation.getMonsterId());

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            try {
                restTemplate.exchange(
                        playerApiUrl + "/api/players/" + invocation.getUsername() + "/monsters/link",
                        HttpMethod.POST,
                        entity,
                        Void.class);
            } catch (HttpClientErrorException e) {
                throw new ResponseStatusException(e.getStatusCode(), "Inventaire plein : libère un emplacement avant d'invoquer");
            }

            invocation.setStatus(InvocationStatus.COMPLETED);
            invocation = invocationRepository.save(invocation);
        }

        return invocation;
    }

    /**
     * Algorithme d'invocation : tire un monstre au sort selon les taux de probabilité.
     * Package-private pour permettre les tests unitaires.
     */
    public BaseMonster pickRandomMonster() {
        List<BaseMonster> monsters = baseMonsterRepository.findAll();
        if (monsters.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Aucun monstre disponible");
        }

        double rand = Math.random() * 100;
        double cumulative = 0;

        for (BaseMonster monster : monsters) {
            cumulative += monster.getInvocationRate();
            if (rand < cumulative) {
                return monster;
            }
        }

        // Sécurité : retourne le dernier si la somme des taux < 100
        return monsters.get(monsters.size() - 1);
    }
}
