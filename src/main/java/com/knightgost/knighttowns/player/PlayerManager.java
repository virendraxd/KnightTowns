package com.knightgost.knighttowns.player;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerManager {

    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private static File file;
    private static FileConfiguration config;

    public static void init(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);

        // Load online players (if plugin reloads)
        Bukkit.getOnlinePlayers().forEach(p -> loadPlayer(p.getUniqueId()));
    }

    public static PlayerData getData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, PlayerData::new);
    }

    public static void loadPlayer(UUID uuid) {
        int xp = config.getInt(uuid + ".xp", 0);
        int level = config.getInt(uuid + ".level", 1);
        PlayerData data = new PlayerData(uuid);
        data.addXP(xp);
        playerDataMap.put(uuid, data);
    }

    public static void savePlayer(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) return;
        config.set(uuid + ".xp", data.getXP());
        config.set(uuid + ".level", data.getLevel());
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public static void saveAll() {
        for (UUID uuid : playerDataMap.keySet()) savePlayer(uuid);
    }
}
