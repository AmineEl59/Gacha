package fr.gacha.playerapi.dto;

import fr.gacha.playerapi.model.ElementType;

public class CreateMonsterRequest {
    private ElementType elementType;
    private int hp;
    private int atk;
    private int def;
    private int vit;

    public CreateMonsterRequest() {}

    public ElementType getElementType() { return elementType; }
    public void setElementType(ElementType elementType) { this.elementType = elementType; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getAtk() { return atk; }
    public void setAtk(int atk) { this.atk = atk; }

    public int getDef() { return def; }
    public void setDef(int def) { this.def = def; }

    public int getVit() { return vit; }
    public void setVit(int vit) { this.vit = vit; }
}
