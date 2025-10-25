package com.knightgost.knighttowns.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.knightgost.knighttowns.utils.TownPerms;

public class Town {

    private final String name;
    private final UUID mayor; // Store UUID instead of Player isChunkClaimed
    private Set<String> claimedChunks = new HashSet<>();
    private Map<UUID, TownRank> members = new HashMap<>();

    public void addMember(UUID uuid, TownRank rank) {
        if (uuid == null || rank == null) return;
        members.put(uuid, rank);
    }

    public void removeMember(UUID uuid) {
        if (uuid == null) return;
        members.remove(uuid);
    }

    public boolean isMember(UUID uuid) {
        if (uuid == null) return false;
        return members.containsKey(uuid);
    }

    public TownRank getRank(UUID uuid) {
        return members.getOrDefault(uuid, TownRank.VISITOR);
    }

    public boolean hasPermission(UUID uuid, TownPermission perm) {
        return TownPerms.hasPermission(getRank(uuid), perm);
    }

    public Map<UUID, TownRank> getMembers() {
        return members;
    }

    public Town(String name, UUID mayor) {
        this.name = name;
        this.mayor = mayor;
    }

    public String getName() {
        return name;
    }

    public UUID getMayorUUID() {
        return mayor;
    }

    public Player getMayorPlayer() {
        return mayor == null ? null : Bukkit.getPlayer(mayor);
    }

    public UUID getTownMasterUUID() {
        // Iterate members and find who has TOWNMASTER rank
        for (Map.Entry<UUID, TownRank> entry : members.entrySet()) {
            if (entry.getValue() == TownRank.TOWNMASTER) return entry.getKey();
        }
        return null;
    }

    public void addClaimedChunk(Chunk chunk) {
        String key = chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
        claimedChunks.add(key);
    }

    public void removeClaimedChunk(Chunk chunk) {
        String key = chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
        claimedChunks.remove(key);
    }

    public Set<String> getClaimedChunks() {
        return claimedChunks;
    }

    public void setClaimedChunks(Set<String> claimedChunks) {
        this.claimedChunks = claimedChunks;
    }

    public Set<Chunk> getClaimedChunksAsObjects() {
        Set<Chunk> chunks = new HashSet<>();
        for (String key : claimedChunks) {
            String[] parts = key.split(",");
            if (parts.length != 3) continue;
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) continue;
            int x = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            chunks.add(world.getChunkAt(x, z));
        }
        return chunks;
    }

    /**
     * Checks if a location is inside the town's claimed area
     */
    public boolean isInsideTown(Location loc) {
        for (Chunk chunk : getClaimedChunksAsObjects()) {
            int x1 = chunk.getX() << 4;
            int z1 = chunk.getZ() << 4;
            int x2 = x1 + 15;
            int z2 = z1 + 15;

            int lx = loc.getBlockX();
            int lz = loc.getBlockZ();

            if (lx >= x1 && lx <= x2 && lz >= z1 && lz <= z2) {
                return true;
            }
        }
        return false;
    }

    public Set<Vector> getClaimedChunkPositions() {
        Set<Vector> positions = new HashSet<>();
        for (String key : claimedChunks) {
            String[] parts = key.split(",");
            if (parts.length != 3) continue;
            int x = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            positions.add(new Vector(x, 0, z));
        }
        return positions;
    }

    public void showChunkBorders(Player player) {
        Set<Vector> positions = getClaimedChunkPositions();

        for (Vector pos : positions) {
            World world = player.getWorld(); // assuming chunks are in same world
            int y = player.getLocation().getBlockY(); // can adjust to your preference

            int startX = pos.getBlockX() << 4; // chunk start X 
            int startZ = pos.getBlockZ() << 4; // chunk start Z
            int endX = startX + 15;
            int endZ = startZ + 15;

            // Place blocks at 4 corners
            setTempBlock(world, startX, y, startZ, Material.GLOWSTONE);
            setTempBlock(world, endX, y, startZ, Material.GLOWSTONE);
            setTempBlock(world, startX, y, endZ, Material.GLOWSTONE);
            setTempBlock(world, endX, y, endZ, Material.GLOWSTONE);

            // Optional: draw edges (lines along X and Z edges)
            for (int x = startX; x <= endX; x++) {
                setTempBlock(world, x, y, startZ, Material.GLOWSTONE);
                setTempBlock(world, x, y, endZ, Material.GLOWSTONE);
            }

            for (int z = startZ; z <= endZ; z++) {
                setTempBlock(world, startX, y, z, Material.GLOWSTONE);
                setTempBlock(world, endX, y, z, Material.GLOWSTONE);
            }
        }
    }

    // Helper to place a temporary block for the player
    private void setTempBlock(World world, int x, int y, int z, Material material) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(material);
        // Optional: schedule block to revert after some time
        // Bukkit.getScheduler().runTaskLater(plugin, () -> block.setType(Material.AIR), 200L);
    }
}
