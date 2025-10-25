package com.knightgost.knighttowns.data;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerXPManager {

    private static File file;
    private static FileConfiguration config;

    public static void setup(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create playerdata.yml");
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static void savePlayer(UUID uuid, int xp, int level) {
        config.set(uuid + ".xp", xp);
        config.set(uuid + ".level", level);
        saveFile();
    }

    public static int getXP(UUID uuid) {
        return config.getInt(uuid + ".xp", 0);
    }

    public static int getLevel(UUID uuid) {
        return config.getInt(uuid + ".level", 1);
    }

    public static void addXP(UUID uuid, int amount) {
        int currentXP = getXP(uuid);
        int newXP = currentXP + amount;
        int level = getLevel(uuid);

        // Example XP requirement: 100 * level
        int requiredXP = level * 100;

        if (newXP >= requiredXP) {
            newXP -= requiredXP;
            level++;
        }

        config.set(uuid + ".xp", newXP);
        config.set(uuid + ".level", level);
        saveFile();
    }

    public static void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
