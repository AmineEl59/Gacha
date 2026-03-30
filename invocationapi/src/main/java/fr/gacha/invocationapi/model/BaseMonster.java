package fr.gacha.invocationapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Modèle de monstre invocable, stocké dans la base invocationapi.
 * Contient les stats de base et le taux d'invocation (en pourcentage).
 */
@Document(collection = "base_monsters")
public class BaseMonster {

    @Id
    private String id;

    private String name;
    private String elementType; // FIRE, WATER, WIND
    private int hp;
    private int atk;
    private int def;
    private int vit;
    private double invocationRate; // ex: 40.0 pour 40%

    public BaseMonster() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

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

    public double getInvocationRate() { return invocationRate; }
    public void setInvocationRate(double invocationRate) { this.invocationRate = invocationRate; }
}
