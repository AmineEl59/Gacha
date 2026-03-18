package com.example.playerapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "players")
public class Player {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private int level;
    private int experience;
    private int experienceThreshold;
    private int maxMonsters;
    private List<String> monsters;

    public Player() {
        this.level = 0;
        this.experience = 0;
        this.experienceThreshold = 50;
        this.maxMonsters = 10;
        this.monsters = new ArrayList<>();
    }

    public Player(String username) {
        this();
        this.username = username;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public int getExperienceThreshold() { return experienceThreshold; }
    public void setExperienceThreshold(int experienceThreshold) { this.experienceThreshold = experienceThreshold; }

    public int getMaxMonsters() { return maxMonsters; }
    public void setMaxMonsters(int maxMonsters) { this.maxMonsters = maxMonsters; }

    public List<String> getMonsters() { return monsters; }
    public void setMonsters(List<String> monsters) { this.monsters = monsters; }
}
