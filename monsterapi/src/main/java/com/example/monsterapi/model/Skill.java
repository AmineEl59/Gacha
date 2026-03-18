package com.example.monsterapi.model;

public class Skill {

    private int baseDamage;
    private double statRatio;
    private StatType statType;
    private int cooldown;
    private int improvementLevel;
    private int maxImprovementLevel;

    public Skill() {}

    public Skill(int baseDamage, double statRatio, StatType statType,
                 int cooldown, int improvementLevel, int maxImprovementLevel) {
        this.baseDamage = baseDamage;
        this.statRatio = statRatio;
        this.statType = statType;
        this.cooldown = cooldown;
        this.improvementLevel = improvementLevel;
        this.maxImprovementLevel = maxImprovementLevel;
    }

    public int getBaseDamage() { return baseDamage; }
    public void setBaseDamage(int baseDamage) { this.baseDamage = baseDamage; }

    public double getStatRatio() { return statRatio; }
    public void setStatRatio(double statRatio) { this.statRatio = statRatio; }

    public StatType getStatType() { return statType; }
    public void setStatType(StatType statType) { this.statType = statType; }

    public int getCooldown() { return cooldown; }
    public void setCooldown(int cooldown) { this.cooldown = cooldown; }

    public int getImprovementLevel() { return improvementLevel; }
    public void setImprovementLevel(int improvementLevel) { this.improvementLevel = improvementLevel; }

    public int getMaxImprovementLevel() { return maxImprovementLevel; }
    public void setMaxImprovementLevel(int maxImprovementLevel) { this.maxImprovementLevel = maxImprovementLevel; }
}
