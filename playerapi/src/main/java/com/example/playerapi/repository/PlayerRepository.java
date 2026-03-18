package com.example.playerapi.repository;

import com.example.playerapi.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PlayerRepository extends MongoRepository<Player, String> {
    Optional<Player> findByUsername(String username);
    boolean existsByUsername(String username);
}
