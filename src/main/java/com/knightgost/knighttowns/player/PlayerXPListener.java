package com.knightgost.knighttowns.player;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.knightgost.knighttowns.data.PlayerXPManager;

public class PlayerXPListener implements Listener {

    private final JavaPlugin plugin;

    public PlayerXPListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // @EventHandler
    // public void onJoin(PlayerJoinEvent e) {
    //     UUID uuid = e.getPlayer().getUniqueId();
    //     PlayerManager.loadPlayer(uuid);
    // }

    // @EventHandler
    // public void onQuit(PlayerQuitEvent e) {
    //     UUID uuid = e.getPlayer().getUniqueId();
    //     PlayerManager.savePlayer(uuid);
    // }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Material block = e.getBlock().getType();

        int xp = switch (block) {
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> 30;
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> 20;
            case IRON_ORE, DEEPSLATE_IRON_ORE, GOLD_ORE, DEEPSLATE_GOLD_ORE -> 10;
            case COPPER_ORE -> 3;
            case OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG, CHERRY_LOG -> 2;
            case STONE -> 1;
            default -> 0;
        };

        if (xp > 0) {
            UUID uuid = player.getUniqueId();

            int oldLevel = PlayerXPManager.getLevel(uuid); // get level before adding XP
            PlayerXPManager.addXP(uuid, xp);
            int newLevel = PlayerXPManager.getLevel(uuid); // get level after adding XP

            player.sendActionBar("§a+" + xp + " XP §7| Level " + newLevel);

            // Check if player leveled up
            if (newLevel > oldLevel) {
                player.sendMessage("§6§lLEVEL UP! §eYou are now Level " + newLevel + "!");
            }
        }
    }


    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e) {
        if (e.getEntity().getKiller() != null) {
            Player killer = e.getEntity().getKiller();
            UUID uuid = killer.getUniqueId();

            int oldLevel = PlayerXPManager.getLevel(uuid);
            PlayerXPManager.addXP(uuid, 50);
            int newLevel = PlayerXPManager.getLevel(uuid);

            killer.sendActionBar("§a+50 XP §7(Kill) | Level " + newLevel);

            if (newLevel > oldLevel) {
                killer.sendMessage("§6§lLEVEL UP! §eYou are now Level " + newLevel + "!");
            }
        }
    }
}
