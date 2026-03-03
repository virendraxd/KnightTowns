package com.knightgost.knighttowns.model;

import java.math.BigDecimal;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private int xp;
    private int level;
    private BigDecimal balance;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.xp = 0;
        this.level = 1;
        this.balance = BigDecimal.ZERO;
    }

    // Constructor for loading existing data
    public PlayerData(UUID uuid, int xp, int level) {
        this.uuid = uuid;
        this.xp = xp;
        this.level = level;
    }

    public UUID getUUID() {
        return uuid;
    }

    public int getXP() {
        return xp;
    }

    public void setLevel(int level) { this.level = level; }

    public int getLevel() {
        return level;
    }

    // Helper to calculate XP required for the next level
    public int getXPToNextLevel() {
        // Formula: 100 + (level * 50)
        return 100 + (level * 50);
    }

    public void addXP(int amount) {
        this.xp += amount;
        checkLevelUp();
    }

    private void checkLevelUp() {
        int requiredXP = getXPToNextLevel();
        while (xp >= requiredXP) {
            xp -= requiredXP;
            level++;
            requiredXP = getXPToNextLevel();
            // In a real plugin, you would save data and notify the player here.
        }
    }

    // Balance methods
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void addBalance(BigDecimal amount) { this.balance = this.balance.add(amount); }
    public void removeBalance(BigDecimal amount) { this.balance = this.balance.subtract(amount); }
}