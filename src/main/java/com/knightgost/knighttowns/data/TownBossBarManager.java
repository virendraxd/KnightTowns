package com.knightgost.knighttowns.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class TownBossBarManager {

    // Active bossbars for players
    private final HashMap<UUID, BossBar> activeBars = new HashMap<>();
    // Players who disabled bossbars
    private final HashSet<UUID> disabledPlayers = new HashSet<>();

    /**
     * Show or update the bossbar for a player
     */
    public void showOrUpdateBossBar(Player player, String townName) {
        if (disabledPlayers.contains(player.getUniqueId())) return;

        if (activeBars.containsKey(player.getUniqueId())) {
            BossBar bossBar = activeBars.get(player.getUniqueId());
            bossBar.setTitle("§6Town: §e" + townName);
        } else {
            BossBar bossBar = Bukkit.createBossBar("§6Town: §e" + townName, BarColor.GREEN, BarStyle.SOLID);
            bossBar.addPlayer(player);
            bossBar.setProgress(1.0);
            activeBars.put(player.getUniqueId(), bossBar);
        }
    }

    /**
     * Remove the bossbar for a player
     */
    public void removeBossBar(Player player) {
        if (activeBars.containsKey(player.getUniqueId())) {
            BossBar bossBar = activeBars.get(player.getUniqueId());
            bossBar.removeAll();
            activeBars.remove(player.getUniqueId());
        }
    }

    /**
     * Toggle bossbar for a player
     */
    public void toggleBossBar(Player player) {
        UUID uuid = player.getUniqueId();
        if (disabledPlayers.contains(uuid)) {
            disabledPlayers.remove(uuid);
            player.sendMessage("§aTown bossbar enabled!");
        } else {
            disabledPlayers.add(uuid);
            removeBossBar(player);
            player.sendMessage("§cTown bossbar disabled!");
        }
    }

    /**
     * Check if bossbar is enabled for a player
     */
    public boolean isEnabled(Player player) {
        return !disabledPlayers.contains(player.getUniqueId());
    }
}
