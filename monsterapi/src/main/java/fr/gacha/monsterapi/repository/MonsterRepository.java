package fr.gacha.monsterapi.repository;

import fr.gacha.monsterapi.model.Monster;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MonsterRepository extends MongoRepository<Monster, String> {
    List<Monster> findByOwnerUsername(String ownerUsername);
}
