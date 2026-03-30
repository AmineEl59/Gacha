package fr.gacha.playerapi.service;

import fr.gacha.playerapi.dto.CreateMonsterRequest;
import fr.gacha.playerapi.model.Player;
import fr.gacha.playerapi.repository.PlayerRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String monsterApiUrl;

    public PlayerService(PlayerRepository playerRepository,
                         @Value("${monster.api.url}") String monsterApiUrl) {
        this.playerRepository = playerRepository;
        this.monsterApiUrl = monsterApiUrl;
    }

    // Calculates the XP threshold required to reach the next level
    private int computeThreshold(int level) {
        if (level <= 1) return 50;
        return (int) Math.round(50 * Math.pow(1.1, level - 1));
    }

    // Récupère le header Authorization de la requête HTTP courante (pour les appels inter-services)
    private String getCurrentAuthHeader() {
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("Authorization");
    }

    // ── CRUD ──────────────────────────────────────────────────────────

    // Crée un profil joueur ; lève CONFLICT si le username est déjà pris
    public Player createPlayer(String username) {
        if (playerRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Player already exists");
        }
        return playerRepository.save(new Player(username));
    }

    // Retourne le profil complet du joueur ; lève NOT_FOUND si inexistant
    public Player getProfile(String username) {
        return playerRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
    }

    // Retourne la liste des IDs de monstres appartenant au joueur
    public List<String> getMonsters(String username) {
        return getProfile(username).getMonsters();
    }

    // Retourne le niveau actuel du joueur
    public int getLevel(String username) {
        return getProfile(username).getLevel();
    }

    // ── Experience ────────────────────────────────────────────────────

    // Ajoute de l'XP au joueur et déclenche des montées de niveau automatiques jusqu'au niveau 50
    public Player gainExperience(String username, int amount) {
        Player player = getProfile(username);
        player.setExperience(player.getExperience() + amount);

        while (player.getLevel() < 50 && player.getExperience() >= player.getExperienceThreshold()) {
            player.setExperience(player.getExperience() - player.getExperienceThreshold());
            doLevelUp(player);
        }

        return playerRepository.save(player);
    }

    // Force une montée de niveau manuelle (reset XP à 0) ; refusé si déjà niveau 50
    public Player levelUp(String username) {
        Player player = getProfile(username);
        if (player.getLevel() >= 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player is already at max level");
        }
        player.setExperience(0);
        doLevelUp(player);
        return playerRepository.save(player);
    }

    // Applique la montée de niveau : +1 niveau, recalcule le seuil XP et augmente le nombre max de monstres
    private void doLevelUp(Player player) {
        player.setLevel(player.getLevel() + 1);
        player.setExperienceThreshold(computeThreshold(player.getLevel()));
        player.setMaxMonsters(10 + player.getLevel());
    }

    // ── Monster management ────────────────────────────────────────────

    // Crée un monstre via monsterapi puis l'ajoute à l'inventaire du joueur ; refusé si inventaire plein
    @SuppressWarnings("unchecked")
    public Player addMonster(String username, CreateMonsterRequest req) {
        Player player = getProfile(username);
        if (player.getMonsters().size() >= player.getMaxMonsters()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Monster list is full");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", getCurrentAuthHeader());
        headers.set("Content-Type", "application/json");
        HttpEntity<CreateMonsterRequest> entity = new HttpEntity<>(req, headers);

        Map<String, Object> monsterResponse = restTemplate.postForObject(
                monsterApiUrl + "/api/monsters", entity, Map.class);

        if (monsterResponse == null || monsterResponse.get("id") == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create monster");
        }

        String monsterId = (String) monsterResponse.get("id");
        player.getMonsters().add(monsterId);
        return playerRepository.save(player);
    }

    /** Rattache un monstre déjà existant (créé par invocationapi) au joueur. */
    public Player linkMonster(String username, String monsterId) {
        Player player = getProfile(username);
        if (player.getMonsters().size() >= player.getMaxMonsters()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Monster list is full");
        }
        player.getMonsters().add(monsterId);
        return playerRepository.save(player);
    }

    // Retire le monstre de l'inventaire du joueur et le supprime dans monsterapi
    public Player removeMonster(String username, String monsterId) {
        Player player = getProfile(username);
        if (!player.getMonsters().remove(monsterId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Monster not found in player list");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", getCurrentAuthHeader());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange(
                monsterApiUrl + "/api/monsters/" + monsterId,
                HttpMethod.DELETE,
                entity,
                Void.class);

        return playerRepository.save(player);
    }
}
