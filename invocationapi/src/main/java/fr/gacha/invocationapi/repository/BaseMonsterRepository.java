package fr.gacha.invocationapi.repository;

import fr.gacha.invocationapi.model.BaseMonster;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BaseMonsterRepository extends MongoRepository<BaseMonster, String> {
}
