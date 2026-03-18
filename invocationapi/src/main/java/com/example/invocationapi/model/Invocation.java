package com.example.invocationapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Base tampon : enregistre chaque invocation avec toutes ses informations.
 * Permet de rejouer les étapes en cas de panne.
 */
@Document(collection = "invocations")
public class Invocation {

    @Id
    private String id;

    private String username;        // joueur qui a invoqué
    private String baseMonsterName; // modèle de monstre tiré au sort
    private String elementType;     // type élémentaire du monstre
    private int hp;
    private int atk;
    private int def;
    private int vit;

    private String monsterId;       // id retourné par monsterapi (null au départ)
    private InvocationStatus status;
    private LocalDateTime createdAt;

    public Invocation() {
        this.status = InvocationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getBaseMonsterName() { return baseMonsterName; }
    public void setBaseMonsterName(String baseMonsterName) { this.baseMonsterName = baseMonsterName; }

    public String getElementType() { return elementType; }
    public void setElementType(String elementType) { this.elementType = elementType; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getAtk() { return atk; }
    public void setAtk(int atk) { this.atk = atk; }

    public int getDef() { return def; }
    public void setDef(int def) { this.def = def; }

    public int getVit() { return vit; }
    public void setVit(int vit) { this.vit = vit; }

    public String getMonsterId() { return monsterId; }
    public void setMonsterId(String monsterId) { this.monsterId = monsterId; }

    public InvocationStatus getStatus() { return status; }
    public void setStatus(InvocationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
