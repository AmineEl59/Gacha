package fr.gacha.invocationapi.model;

public enum InvocationStatus {
    PENDING,          // invocation enregistrée, rien n'a été fait
    MONSTER_CREATED,  // monstre créé dans monsterapi
    COMPLETED         // monstre ajouté au joueur
}
