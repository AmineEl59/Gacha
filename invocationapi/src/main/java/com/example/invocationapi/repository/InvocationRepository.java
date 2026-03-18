package com.example.invocationapi.repository;

import com.example.invocationapi.model.Invocation;
import com.example.invocationapi.model.InvocationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InvocationRepository extends MongoRepository<Invocation, String> {
    List<Invocation> findByStatusNot(InvocationStatus status);
}
