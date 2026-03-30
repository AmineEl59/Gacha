package fr.gacha.invocationapi.repository;

import fr.gacha.invocationapi.model.Invocation;
import fr.gacha.invocationapi.model.InvocationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InvocationRepository extends MongoRepository<Invocation, String> {
    List<Invocation> findByStatusNot(InvocationStatus status);
    List<Invocation> findByUsername(String username);
}
