package com.example.combatapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "combat_logs")
public class CombatLog {

    @Id
    private String id;

    private int combatNumber;
    private String monster1Id;
    private String monster2Id;
    private String winnerId;      // id du monstre vainqueur (null si timeout)
    private String winnerOwner;   // username du joueur vainqueur
    private List<TurnLog> turns;
    private LocalDateTime createdAt;

    public CombatLog() {
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getCombatNumber() { return combatNumber; }
    public void setCombatNumber(int combatNumber) { this.combatNumber = combatNumber; }

    public String getMonster1Id() { return monster1Id; }
    public void setMonster1Id(String monster1Id) { this.monster1Id = monster1Id; }

    public String getMonster2Id() { return monster2Id; }
    public void setMonster2Id(String monster2Id) { this.monster2Id = monster2Id; }

    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }

    public String getWinnerOwner() { return winnerOwner; }
    public void setWinnerOwner(String winnerOwner) { this.winnerOwner = winnerOwner; }

    public List<TurnLog> getTurns() { return turns; }
    public void setTurns(List<TurnLog> turns) { this.turns = turns; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
