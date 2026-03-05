package com.knightgost.knighttowns.model;

import org.bukkit.entity.Entity;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TownRaid {
    private final Town town;
    private int currentWave;
    private int totalWaves;
    private final Set<UUID> activeMobs = new HashSet<>();
    private boolean active;
    private final long startTime;
    private final RaidDifficulty difficulty;
    private final Set<UUID> contributors = new HashSet<>();

    public TownRaid(Town town, int totalWaves, RaidDifficulty difficulty) {
        this.town = town;
        this.totalWaves = totalWaves;
        this.difficulty = difficulty;
        this.currentWave = 0;
        this.active = true;
        this.startTime = System.currentTimeMillis();
    }

    public void addContributor(UUID uuid) {
        contributors.add(uuid);
    }

    public Set<UUID> getContributors() {
        return contributors;
    }

    public RaidDifficulty getDifficulty() {
        return difficulty;
    }

    public Town getTown() {
        return town;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public void setCurrentWave(int currentWave) {
        this.currentWave = currentWave;
    }

    public int getTotalWaves() {
        return totalWaves;
    }

    public void setTotalWaves(int totalWaves) {
        this.totalWaves = totalWaves;
    }

    public Set<UUID> getActiveMobs() {
        return activeMobs;
    }

    public void addMob(Entity entity) {
        activeMobs.add(entity.getUniqueId());
    }

    public void removeMob(UUID uuid) {
        activeMobs.remove(uuid);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getRemainingMobs() {
        return activeMobs.size();
    }
}
