package com.example.invocationapi.model;

public enum InvocationStatus {
    PENDING,          // invocation enregistrée, rien n'a été fait
    MONSTER_CREATED,  // monstre créé dans monsterapi
    COMPLETED         // monstre ajouté au joueur
}
