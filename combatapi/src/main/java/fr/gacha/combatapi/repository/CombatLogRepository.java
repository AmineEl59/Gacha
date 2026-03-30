package fr.gacha.combatapi.repository;

import fr.gacha.combatapi.model.CombatLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CombatLogRepository extends MongoRepository<CombatLog, String> {
    Optional<CombatLog> findByCombatNumber(int combatNumber);
}
