package com.knightgost.knighttowns.listeners;

import com.knightgost.knighttowns.manager.PlayerManager;
import com.knightgost.knighttowns.utils.UpdateChecker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final UpdateChecker updateChecker;

    public PlayerListener(UpdateChecker updateChecker) {
        this.updateChecker = updateChecker;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerManager.loadPlayer(event.getPlayer().getUniqueId());

        Player player = event.getPlayer();

        if (player.isOp()) {
            updateChecker.notifyPlayer(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PlayerManager.savePlayer(event.getPlayer().getUniqueId());
    }
}
