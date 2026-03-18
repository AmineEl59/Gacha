package com.example.combatapi.model;

import java.util.ArrayList;
import java.util.List;

public class TurnLog {

    private int turnNumber;
    private List<String> actions;

    public TurnLog() {
        this.actions = new ArrayList<>();
    }

    public TurnLog(int turnNumber) {
        this();
        this.turnNumber = turnNumber;
    }

    public void addAction(String action) {
        this.actions.add(action);
    }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }

    public List<String> getActions() { return actions; }
    public void setActions(List<String> actions) { this.actions = actions; }
}
