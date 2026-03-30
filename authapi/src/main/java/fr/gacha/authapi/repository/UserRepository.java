package fr.gacha.authapi.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import fr.gacha.authapi.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
