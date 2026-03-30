package fr.gacha.playerapi.dto;

public class ExperienceRequest {
    private int amount;

    public ExperienceRequest() {}
    public ExperienceRequest(int amount) { this.amount = amount; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
}
