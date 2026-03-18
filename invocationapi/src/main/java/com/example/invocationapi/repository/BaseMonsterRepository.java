package com.example.invocationapi.repository;

import com.example.invocationapi.model.BaseMonster;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BaseMonsterRepository extends MongoRepository<BaseMonster, String> {
}
