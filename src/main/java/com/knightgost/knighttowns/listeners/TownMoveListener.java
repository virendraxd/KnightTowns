package com.knightgost.knighttowns.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;
import com.knightgost.knighttowns.data.*;


public class TownMoveListener implements Listener {

    private final TownBossBarManager bossBarManager;

    public TownMoveListener(TownBossBarManager bossBarManager) {
        this.bossBarManager = bossBarManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String currentTown = TownManager.getTownAt(player.getLocation()); // static method
        if (currentTown != null) {
            bossBarManager.showOrUpdateBossBar(player, currentTown);
        } else {
            bossBarManager.removeBossBar(player);
        }
    }
}
