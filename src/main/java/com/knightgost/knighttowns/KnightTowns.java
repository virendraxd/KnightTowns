package com.knightgost.knighttowns;

import org.bukkit.plugin.java.JavaPlugin;

import com.knightgost.knighttowns.commands.TownCommand;
import com.knightgost.knighttowns.data.PlayerXPManager;
import com.knightgost.knighttowns.data.TownBossBarManager;
import com.knightgost.knighttowns.data.TownManager;
import com.knightgost.knighttowns.listeners.TownProtectionListener;
import com.knightgost.knighttowns.player.PlayerXPListener;

public class KnightTowns extends JavaPlugin {

    private static KnightTowns instance;
    public TownBossBarManager bossBarManager;

    @Override
    public void onEnable() {
        instance = this;

        // Setup player XP data and towns
        PlayerXPManager.setup(this);
        TownManager.loadTownsFromFile(this);
        bossBarManager = new TownBossBarManager();

        // Register the /town command
        if (getCommand("town") != null) {
            getCommand("town").setExecutor(new TownCommand(this));
        } else {
            getLogger().warning("Town command not found in plugin.yml!");
        }
        
        // Register event listener for updating bossbar when players move
        getServer().getPluginManager().registerEvents(new PlayerXPListener(this), this);
        getServer().getPluginManager().registerEvents(new TownProtectionListener(this), this);
        // getServer().getPluginManager().registerEvents(new TownMoveListener(bossBarManager), this);

        getLogger().info("KnightTowns has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("KnightTowns has been disabled!");
    }

    // Static method to access plugin instance
    public static KnightTowns getInstance() {
        return instance;
    }
}
