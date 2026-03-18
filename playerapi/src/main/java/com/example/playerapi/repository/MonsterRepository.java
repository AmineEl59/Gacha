package com.example.playerapi.repository;

import com.example.playerapi.model.Monster;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MonsterRepository extends MongoRepository<Monster, String> {
    List<Monster> findByOwnerUsername(String ownerUsername);
}
