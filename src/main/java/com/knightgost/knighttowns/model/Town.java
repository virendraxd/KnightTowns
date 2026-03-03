package com.knightgost.knighttowns.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.knightgost.knighttowns.utils.TownPerms;

public class Town {

    private final String name;
    private final UUID mayor; // Store UUID instead of Player isChunkClaimed
    private final String creationDate;
    private Set<String> claimedChunks = new HashSet<>();
    private final Map<UUID, TownRank> members = new HashMap<>();

    public void addMember(UUID uuid, TownRank rank) {
        if (uuid == null || rank == null)
            return;
        members.put(uuid, rank);
    }

    public void removeMember(UUID uuid) {
        if (uuid == null)
            return;
        members.remove(uuid);
    }

    public boolean isMember(UUID uuid) {
        if (uuid == null)
            return false;
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
        this.creationDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());

        members.put(mayor, TownRank.MAYOR);
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

    public String getMayorName() {
        // Check all members for MAYOR rank
        for (Map.Entry<UUID, TownRank> entry : members.entrySet()) {
            if (entry.getValue() == TownRank.MAYOR) {
                OfflinePlayer mayorPlayer = Bukkit.getOfflinePlayer(entry.getKey());
                if (mayorPlayer != null && mayorPlayer.getName() != null) {
                    return mayorPlayer.getName();
                } else {
                    return entry.getKey().toString().substring(0, 8);
                }
            }
        }

        // If no mayor is assigned
        return "Unknown";
    }

    public String getCreationDate() {
        return creationDate;
    }

    public UUID getTownMasterUUID() {
        // Iterate members and find who has TOWNMASTER rank
        for (Map.Entry<UUID, TownRank> entry : members.entrySet()) {
            if (entry.getValue() == TownRank.TOWNMASTER)
                return entry.getKey();
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
            if (parts.length != 3)
                continue;
            World world = Bukkit.getWorld(parts[0]);
            if (world == null)
                continue;
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
            if (parts.length != 3)
                continue;
            int x = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            positions.add(new Vector(x, 0, z));
        }
        return positions;
    }

}
