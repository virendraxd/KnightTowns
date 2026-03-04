package com.knightgost.knighttowns.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private final String versionURL;
    private String latestVersion;

    public UpdateChecker(JavaPlugin plugin, String versionURL) {
        this.plugin = plugin;
        this.versionURL = versionURL;
    }

    public void checkForUpdates() {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(versionURL).openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                latestVersion = reader.readLine().trim();
                reader.close();

                String currentVersion = plugin.getPluginMeta().getVersion();

                if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                    plugin.getLogger().warning("[KnightTowns] A new version " + latestVersion
                    + " is available, you are running " + currentVersion);
                }

            } catch (Exception ignored) {
                // Silent fail (no internet or error)
            }
        });
    }

    public void notifyPlayer(Player player) {

        if (latestVersion == null)
            return;

        String currentVersion = plugin.getPluginMeta().getVersion();

        if (!currentVersion.equalsIgnoreCase(latestVersion)) {
            player.sendMessage("§6§l[KnightTowns] §6A new version " + latestVersion
                    + " is available, you are running " + currentVersion);
        }
    }
}