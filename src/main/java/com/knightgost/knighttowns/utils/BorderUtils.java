package com.knightgost.knighttowns.utils;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.knightgost.knighttowns.data.Town;

public class BorderUtils {

    // Show town border with glowstone corners and gold markers
    // 1 gold block adjacent to each corner, then every 8 blocks
    public static void showTownBorder(JavaPlugin plugin, Player player, Town town, int seconds) {
        if (town == null || town.getClaimedChunks().isEmpty()) {
            player.sendMessage("§cThis town has no claimed chunks to show.");
            return;
        }

        Set<String> claimed = town.getClaimedChunks();
        Set<Location> previewBlocks = new HashSet<>();

        World world = null;
        Set<String> claimedSet = new HashSet<>(claimed);

        // Iterate through all claimed chunks
        for (String key : claimed) {
            String[] parts = key.split(",");
            if (parts.length != 3) continue;

            world = Bukkit.getWorld(parts[0]);
            if (world == null) continue;

            int cx = Integer.parseInt(parts[1]);
            int cz = Integer.parseInt(parts[2]);

            int startX = cx << 4; // Chunk start X
            int startZ = cz << 4; // Chunk start Z
            int endX = startX + 15; // Chunk end X
            int endZ = startZ + 15; // Chunk end Z

            // Check neighboring chunks to determine outer edges
            boolean north = !claimedSet.contains(world.getName() + "," + cx + "," + (cz - 1));
            boolean south = !claimedSet.contains(world.getName() + "," + cx + "," + (cz + 1));
            boolean west = !claimedSet.contains(world.getName() + "," + (cx - 1) + "," + cz);
            boolean east = !claimedSet.contains(world.getName() + "," + (cx + 1) + "," + cz);

            // Place glowstone at corners where two edges meet
            if (north && west) {
                Location loc = getSurface(world, startX, startZ);
                player.sendBlockChange(loc, Material.GLOWSTONE.createBlockData());
                previewBlocks.add(loc);
            }
            if (north && east) {
                Location loc = getSurface(world, endX, startZ);
                player.sendBlockChange(loc, Material.GLOWSTONE.createBlockData());
                previewBlocks.add(loc);
            }
            if (south && west) {
                Location loc = getSurface(world, startX, endZ);
                player.sendBlockChange(loc, Material.GLOWSTONE.createBlockData());
                previewBlocks.add(loc);
            }
            if (south && east) {
                Location loc = getSurface(world, endX, endZ);
                player.sendBlockChange(loc, Material.GLOWSTONE.createBlockData());
                previewBlocks.add(loc);
            }

            // North edge (top)
            if (north) {
                // Place gold adjacent to west corner
                if (west) {
                    Location loc = getSurface(world, startX + 1, startZ);
                    player.sendBlockChange(loc, Material.GOLD_BLOCK.createBlockData());
                    previewBlocks.add(loc);
                }
                
                // Place gold blocks every 8 blocks (starting from startX + 8)
                for (int x = startX + 8; x < endX; x += 8) {
                    Location loc = getSurface(world, x, startZ);
                    player.sendBlockChange(loc, Material.GOLD_BLOCK.createBlockData());
                    previewBlocks.add(loc);
                }
                
                // Place gold adjacent to east corner
                if (east) {
                    Location loc = getSurface(world, endX - 1, startZ);
                    player.sendBlockChange(loc, Material.GOLD_BLOCK.createBlockData());
                    previewBlocks.add(loc);
                }
            }

            // South edge (bottom)
            if (south) {
                // Place gold adjacent to west corner
                if (west) {
                    Location loc = getSurface(world, startX + 1, endZ);
                    player.sendBlockChange(loc, Material.GOLD_BLOCK.createBlockData());
                    previewBlocks.add(loc);
                }
                
                // Place gold blocks every 8 blocks
                for (int x = startX + 8; x < endX; x += 8) {
                    Location loc = getSurface(world, x, endZ);
                    player.sendBlockChange(loc, Material.GOLD_BLOCK.createBlockData());
                    previewBlocks.add(loc);
                }
                
                // Place gold adjacent to east corner
                if (east) {
                    Location loc = getSurface(world, endX - 1, endZ);
                    player.sendBlockChange(loc, Material.GOLD_BLOCK.createBlockData());
                    previewBlocks.add(loc);
                }
            }

            // West edge (left)
            if (west) {
                // Place gold adjacent to north corner
                if (north) {
                    Location loc = getSurface(world, startX, startZ + 1);
                    player.sendBlockChange(loc, Material.GOLD_BLOCK.createBlockData());
                    previewBlocks.add(loc);
                }
                
                // Place gold blocks every 8 blocks
                for (int z = startZ + 8; z < endZ; z += 8) {
                    Location loc = getSurface(world, startX, z);
                    player.sendBlockChange(loc, Material.GOLD_BLOCK.createBlockData());
                    previewBlocks.add(loc);
                }
                
                // Place gold adjacent to south corner
                if (south) {
                    Location loc = getSurface(world, startX, endZ - 1);
                    player.sendBlockChange(loc, Material.GOLD_BLOCK.createBlockData());
                    previewBlocks.add(loc);
                }
            }

            // East edge (right)
            if (east) {
                // Place gold adjacent to north corner
                if (north) {
                    Location loc = getSurface(world, endX, startZ + 1);
                    player.sendBlockChange(loc, Material.GOLD_BLOCK.createBlockData());
                    previewBlocks.add(loc);
                }
                
                // Place gold blocks every 8 blocks
                for (int z = startZ + 8; z < endZ; z += 8) {
                    Location loc = getSurface(world, endX, z);
                    player.sendBlockChange(loc, Material.GOLD_BLOCK.createBlockData());
                    previewBlocks.add(loc);
                }
                
                // Place gold adjacent to south corner
                if (south) {
                    Location loc = getSurface(world, endX, endZ - 1);
                    player.sendBlockChange(loc, Material.GOLD_BLOCK.createBlockData());
                    previewBlocks.add(loc);
                }
            }
        }

        // Revert blocks to original state after delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Location loc : previewBlocks) {
                player.sendBlockChange(loc, loc.getBlock().getBlockData());
            }
        }, seconds * 20L);
    }

    // Find surface location at X,Z
    private static Location getSurface(World world, int x, int z) {
        int y = world.getHighestBlockYAt(x, z);
        Block block = world.getBlockAt(x, y - 1, z);
        return block.getLocation().add(0, 1, 0);
    }
}