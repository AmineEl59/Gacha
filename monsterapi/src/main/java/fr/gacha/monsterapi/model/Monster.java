package fr.gacha.monsterapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "monsters")
public class Monster {

    @Id
    private String id;

    private String ownerUsername;
    private ElementType elementType;

    private int level;
    private int experience;
    private int experienceThreshold;
    private int skillPoints;

    private int hp;
    private int atk;
    private int def;
    private int vit;

    private List<Skill> skills;

    public Monster() {
        this.level = 1;
        this.experience = 0;
        this.experienceThreshold = 50;
        this.skillPoints = 0;
        this.skills = new ArrayList<>();
    }

    public Monster(String ownerUsername, ElementType elementType, int hp, int atk, int def, int vit) {
        this();
        this.ownerUsername = ownerUsername;
        this.elementType = elementType;
        this.hp = hp;
        this.atk = atk;
        this.def = def;
        this.vit = vit;
        this.skills.add(new Skill(10, 0.5, StatType.ATK, 2, 0, 5));
        this.skills.add(new Skill(8, 0.3, StatType.DEF, 3, 0, 5));
        this.skills.add(new Skill(12, 0.4, StatType.VIT, 4, 0, 5));
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public ElementType getElementType() { return elementType; }
    public void setElementType(ElementType elementType) { this.elementType = elementType; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public int getExperienceThreshold() { return experienceThreshold; }
    public void setExperienceThreshold(int experienceThreshold) { this.experienceThreshold = experienceThreshold; }

    public int getSkillPoints() { return skillPoints; }
    public void setSkillPoints(int skillPoints) { this.skillPoints = skillPoints; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getAtk() { return atk; }
    public void setAtk(int atk) { this.atk = atk; }

    public int getDef() { return def; }
    public void setDef(int def) { this.def = def; }

    public int getVit() { return vit; }
    public void setVit(int vit) { this.vit = vit; }

    public List<Skill> getSkills() { return skills; }
    public void setSkills(List<Skill> skills) { this.skills = skills; }
}
