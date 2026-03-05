package com.knightgost.knighttowns.model;

public enum RaidDifficulty {
    EASY("Easy", 1.0, 50),
    NORMAL("Normal", 1.5, 100),
    HARD("Hard", 2.0, 200);

    private final String displayName;
    private final double multiplier;
    private final int baseXP;

    RaidDifficulty(String displayName, double multiplier, int baseXP) {
        this.displayName = displayName;
        this.multiplier = multiplier;
        this.baseXP = baseXP;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public int getBaseXP() {
        return baseXP;
    }
}
