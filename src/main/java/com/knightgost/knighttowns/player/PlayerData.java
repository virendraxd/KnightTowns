package com.knightgost.knighttowns.player;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private int xp;
    private int level;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.xp = 0;
        this.level = 1;
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
        }
    }

    private int getXPToNextLevel() {
        // You can tweak this formula (e.g., exponential or linear)
        return 100 + (level * 50);
    }
}
