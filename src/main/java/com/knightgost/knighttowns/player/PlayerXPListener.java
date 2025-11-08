package com.knightgost.knighttowns.player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.knightgost.knighttowns.data.PlayerXPManager;

public class PlayerXPListener implements Listener {

    private final JavaPlugin plugin;
    private final Set<String> placedBlocks = new HashSet<>();

    public PlayerXPListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        placedBlocks.add(block.getLocation().toString());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        // Prevent XP farming from placed blocks
        if (placedBlocks.contains(block.getLocation().toString())) {
            placedBlocks.remove(block.getLocation().toString());
            return; // Don't give XP for placed blocks
        }

        Material type = block.getType();

        int xp = switch (type) {
            // Precious ores
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> 30;
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> 25;
            case ANCIENT_DEBRIS -> 40;

            // Medium-tier ores
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> 12;
            case IRON_ORE, DEEPSLATE_IRON_ORE -> 10;
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> 8;
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> 6;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> 4;
            case COAL_ORE, DEEPSLATE_COAL_ORE -> 3;

            // Overworld mining blocks
            case STONE, COBBLESTONE, ANDESITE, DIORITE, GRANITE -> 1;
            case OBSIDIAN -> 15;
            case TUFF, CALCITE -> 2;

            // Logs and woods
            case OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG,
                 DARK_OAK_LOG, CHERRY_LOG, MANGROVE_LOG, BAMBOO_BLOCK -> 2;
            case CRIMSON_STEM, WARPED_STEM -> 3;

            // Farming
            case WHEAT, CARROTS, POTATOES, BEETROOTS, SUGAR_CANE -> 1;
            case PUMPKIN, MELON, CACTUS, BAMBOO, MANGROVE_ROOTS -> 2;
            case HAY_BLOCK -> 3;

            // Adventure
            case CHEST, BARREL -> 5;
            case SPAWNER -> 50;

            default -> 0;
        };

        if (xp > 0) {
            UUID uuid = player.getUniqueId();

            int oldLevel = PlayerXPManager.getLevel(uuid);
            PlayerXPManager.addXP(uuid, xp);
            int newLevel = PlayerXPManager.getLevel(uuid);

            player.sendActionBar("§a+" + xp + " XP §7| Level " + newLevel);

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
