package com.knightgost.knighttowns.manager;

import com.knightgost.knighttowns.model.Town;
import com.knightgost.knighttowns.model.TownRank;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TownManager {

    private static final Map<String, Town> towns = new HashMap<>();

    public static Town getTown(String name) {
        if (name == null)
            return null;
        return towns.get(name.toLowerCase());
    }

    public static boolean townExists(String name) {
        return towns.containsKey(name.toLowerCase());
    }

    public static void addTown(Town town) {
        if (town != null)
            towns.put(town.getName().toLowerCase(), town);
    }

    public static void removeTown(String name) {
        if (name != null)
            towns.remove(name.toLowerCase());
    }

    public static Collection<Town> getAllTowns() {
        return towns.values();
    }

    public static String getTownAt(Location loc) {
        for (Town town : towns.values()) {
            // Assuming Town has a method to check if a location is inside claimed chunks
            if (town.isInsideTown(loc)) {
                return town.getName();
            }
        }
        return null;
    }

    /**
     * Calculates the town level based on its current size or resources.
     * * @param town The Town object.
     * 
     * @return The calculated level (or 0 if the town is null).
     */
    public static int getTownLevel(Town town) {
        if (town == null) {
            return 0;
        }

        // --- CUSTOM LEVELING LOGIC ---
        // EXAMPLE: Level increases by 1 for every 5 members, starting at Level 1 for
        // 1-4 members.
        int population = town.getMembers().size();
        if (population == 0)
            return 0;

        // Formula: Level = (Population - 1) / 5 + 1
        return ((population - 1) / 5) + 1;

        // If the Town class had a dedicated level field, you would use:
        // return town.getLevel();
        // -----------------------------
    }

    // ===== Helper: get the town a player (by UUID) is mayor of =====
    public static Town getPlayerTown(UUID playerUUID) {
        if (playerUUID == null)
            return null;
        for (Town town : towns.values()) {
            if (playerUUID.equals(town.getMayorUUID())) {
                return town;
            }
        }
        return null;
    }

    public static Town getTownByPlayer(UUID playerUUID) {
        if (playerUUID == null)
            return null;

        for (Town town : towns.values()) {
            if (town.getMembers().containsKey(playerUUID)) {
                return town;
            }
        }

        return null;
    }

    // ===== Saving a town to file =====
    public static void saveTownToFile(JavaPlugin plugin, Town town) {
        if (town == null || plugin == null)
            return;

        try {
            File file = new File(plugin.getDataFolder(), "towns.yml");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String path = town.getName().toLowerCase();

            // Townmaster UUID (highest rank)
            UUID townMasterUUID = town.getTownMasterUUID(); // implement this method in Town class
            if (townMasterUUID != null) {
                config.set(path + ".townmaster", townMasterUUID.toString());
            } else {
                config.set(path + ".townmaster", "none");
            }

            // Creation date
            config.set(path + ".datecreated", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));

            // Claimed chunks
            Set<String> claimedChunks = town.getClaimedChunks();
            List<String> chunkList = new ArrayList<>();
            if (claimedChunks != null && !claimedChunks.isEmpty()) {
                chunkList.addAll(claimedChunks); // already stored as "world,x,z"
            }
            config.set(path + ".claimedChunks", chunkList);

            // Members & ranks
            ConfigurationSection membersSection = config.createSection(path + ".members");
            for (Map.Entry<UUID, TownRank> entry : town.getMembers().entrySet()) {
                membersSection.set(entry.getKey().toString(), entry.getValue().name());
            }

            config.save(file);

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save town: " + town.getName());
            e.printStackTrace();
        }
    }

    // ===== Load all towns from file =====
    public static void loadTownsFromFile(JavaPlugin plugin) {
        if (plugin == null)
            return;

        File file = new File(plugin.getDataFolder(), "towns.yml");
        if (!file.exists())
            return;

        // ✅ CLEAR MEMORY FIRST
        towns.clear();

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String townName : config.getKeys(false)) {
            String path = townName.toLowerCase();

            String townMasterStr = config.getString(path + ".townmaster");
            UUID townMasterUUID = null;

            if (townMasterStr != null && !townMasterStr.equals("none")) {
                try {
                    townMasterUUID = UUID.fromString(townMasterStr);
                } catch (IllegalArgumentException ignored) {
                }
            }

            Town town = new Town(townName, townMasterUUID);

            // claimed chunks
            for (String c : config.getStringList(path + ".claimedChunks")) {
                String[] parts = c.split(",");
                if (parts.length != 3)
                    continue;

                World world = Bukkit.getWorld(parts[0]);
                if (world == null)
                    continue;

                try {
                    int x = Integer.parseInt(parts[1]);
                    int z = Integer.parseInt(parts[2]);
                    town.addClaimedChunk(world.getChunkAt(x, z));
                } catch (NumberFormatException ignored) {
                }
            }

            // members
            ConfigurationSection members = config.getConfigurationSection(path + ".members");
            if (members != null) {
                for (String uuidStr : members.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        TownRank rank = TownRank.valueOf(members.getString(uuidStr, "VISITOR"));
                        town.addMember(uuid, rank);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            towns.put(path, town);
        }

        plugin.getLogger().info("Loaded " + towns.size() + " towns.");
    }

    public static Town getTownByChunk(Chunk chunk) {
        String key = chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
        for (Town town : towns.values()) {
            if (town.getClaimedChunks().contains(key)) {
                return town;
            }
        }
        return null;
    }

    public static Town getTownByLocation(Location loc) {
        for (Town town : towns.values()) {
            if (town.isInsideTown(loc)) {
                return town;
            }
        }
        return null;
    }

    public static void deleteTown(JavaPlugin plugin, String name) {
        if (plugin == null || name == null)
            return;

        String key = name.toLowerCase();

        // ✅ Remove from memory FIRST
        towns.remove(key);

        // ✅ Remove from file
        File file = new File(plugin.getDataFolder(), "towns.yml");
        if (!file.exists())
            return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set(key, null);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to delete town: " + key);
            e.printStackTrace();
        }
    }

}