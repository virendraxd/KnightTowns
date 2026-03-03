package com.knightgost.knighttowns.manager;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;

public class ShopManager {

    private final File file;
    private FileConfiguration config;

    public ShopManager(File folder) {
        this.file = new File(folder, "shop.yml");

        // Ensure plugin folder exists
        if (!folder.exists())
            folder.mkdirs();

        // Only create a new file if it doesn't exist
        if (!file.exists()) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("shop.yml")) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Now safely load and merge defaults
        reload();
    }

    /** Reloads config and merges new defaults without overwriting user values */
    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);

        // Load defaults from jar (for future updates)
        try (InputStream defStream = getClass().getClassLoader().getResourceAsStream("shop.yml")) {

            if (defStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));

                config.setDefaults(defConfig);
                config.options().copyDefaults(true);
                save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Saves shop.yml safely */
    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    // ------------------------------------------------
    // CATEGORY METHODS
    // ------------------------------------------------

    public List<String> getCategories() {
        if (config.getConfigurationSection("categories") == null)
            return new ArrayList<>();

        return new ArrayList<>(config.getConfigurationSection("categories").getKeys(false));
    }

    public Material getCategoryIcon(String category) {
        return Material.matchMaterial(
                config.getString("categories." + category + ".material", "BARRIER"));
    }

    public String getCategoryName(String category) {
        return config.getString("categories." + category + ".name", category);
    }

    public int getCategorySlot(String category) {
        return config.getInt("categories." + category + ".slot", 0);
    }

    public List<String> getCategoryLore(String category) {
        return config.getStringList("categories." + category + ".lore");
    }

    // ------------------------------------------------
    // ITEM METHODS
    // ------------------------------------------------

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getItems(String category) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) config.getList("items." + category);

        return list != null ? list : new ArrayList<>();
    }
}
