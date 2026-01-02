package com.knightgost.knighttowns.service;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private int xp;
    private int level;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        // When loaded/created, default to 0 XP and Lvl 1
        this.xp = 0;
        this.level = 1;
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
}