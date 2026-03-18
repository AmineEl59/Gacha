package com.example.authapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.authapi.model.Token;

public interface TokenRepository extends MongoRepository<Token, String> {
    Optional<Token> findByValue(String value);
    Optional<Token> findByValueAndRevokedFalse(String value);
    List<Token> findAllByUserIdAndRevokedFalse(String userId);
}
