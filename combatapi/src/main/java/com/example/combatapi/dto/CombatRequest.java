package com.example.combatapi.dto;

public class CombatRequest {
    private String monster1Id;
    private String monster2Id;

    public CombatRequest() {}

    public String getMonster1Id() { return monster1Id; }
    public void setMonster1Id(String monster1Id) { this.monster1Id = monster1Id; }

    public String getMonster2Id() { return monster2Id; }
    public void setMonster2Id(String monster2Id) { this.monster2Id = monster2Id; }
}
