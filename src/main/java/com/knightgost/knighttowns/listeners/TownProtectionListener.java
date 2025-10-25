package com.knightgost.knighttowns.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.knightgost.knighttowns.data.Town;
import com.knightgost.knighttowns.data.TownManager;
import com.knightgost.knighttowns.data.TownRank;

/**
 * Protects claimed town chunks from griefing.
 * - Visitors (or null-rank) cannot place/break/interact in other towns.
 * - OPs and players with knighttowns.bypass permission bypass protection.
 * - Explosions do not break blocks in claimed towns.
 */
public class TownProtectionListener implements Listener {

    private final JavaPlugin plugin;

    public TownProtectionListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Central permission check.
     * Returns true if player is allowed to modify (place/break/interact) at the given location.
     */
    private boolean canModify(Player player, Location loc) {
        // OP bypass
        if (player.isOp()) return true;

        // Permission bypass
        if (player.hasPermission("knighttowns.bypass")) return true;

        Town town = TownManager.getTownByLocation(loc);
        if (town == null) return true; // not inside any town => allowed

        UUID playerUUID = player.getUniqueId();
        TownRank rank = town.getRank(playerUUID);

        // Treat null rank as VISITOR
        if (rank == null) rank = TownRank.VISITOR;

        // Only ranks better than VISITOR may modify
        return rank != TownRank.VISITOR;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        if (!canModify(player, loc)) {
            player.sendMessage("§cVisitors cannot break blocks in another town!");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        if (!canModify(player, loc)) {
            player.sendMessage("§cVisitors cannot place blocks in another town!");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null) return;

        Material type = event.getClickedBlock().getType();
        if (!type.isInteractable()) return;

        Location loc = event.getClickedBlock().getLocation();
        if (!canModify(player, loc)) {
            player.sendMessage("§cVisitors cannot interact with blocks in another town!");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event) {
        List<Block> toRemove = new ArrayList<>();
        for (Block block : event.blockList()) {
            Town town = TownManager.getTownByLocation(block.getLocation());
            if (town != null) {
                toRemove.add(block);
            }
        }
        event.blockList().removeAll(toRemove);
    }
}