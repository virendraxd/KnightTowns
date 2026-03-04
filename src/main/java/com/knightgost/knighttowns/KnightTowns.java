package com.knightgost.knighttowns;

import com.knightgost.knighttowns.commands.TownCommand;
import com.knightgost.knighttowns.gui.SellChestGUI;
import com.knightgost.knighttowns.gui.ShopGUI;
import com.knightgost.knighttowns.gui.TownGUI;
import com.knightgost.knighttowns.listeners.*;
import com.knightgost.knighttowns.manager.*;
import com.knightgost.knighttowns.model.TownBossBarManager;
import com.knightgost.knighttowns.utils.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

public class KnightTowns extends JavaPlugin {

    private ShopManager shopManager;
    private CurrencyManager currencyManager;
    public TownBossBarManager bossBarManager;

    @Override
    public void onEnable() {
        PlayerManager.init(this);
        saveDefaultConfig();

        // version checker
        String versionURL = "https://raw.githubusercontent.com/virendraxd/KnightTowns/main/version.txt";

        UpdateChecker updateChecker = new UpdateChecker(this, versionURL);
        updateChecker.checkForUpdates();

        currencyManager = new CurrencyManager();
        shopManager = new ShopManager(getDataFolder());

        TownGUI townGUI = new TownGUI(this, currencyManager);
        ShopGUI shopGUI = new ShopGUI(this, shopManager, currencyManager);
        SellChestGUI sellChestGUI = new SellChestGUI();

        // =========================
        // OTHER SETUP
        // =========================
        PlayerXPManager.setup(this);
        TownManager.loadTownsFromFile(this);
        bossBarManager = new TownBossBarManager();

        // =========================
        // COMMANDS
        // =========================
        if (getCommand("town") != null) {
            getCommand("town").setExecutor(
                    new TownCommand(this,
                            townGUI,
                            shopGUI,
                            sellChestGUI));
        } else {
            getLogger().warning("Town command not found in plugin.yml!");
        }

        // Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerXPListener(this), this);
        getServer().getPluginManager().registerEvents(new TownGUIListener(townGUI), this);
        getServer().getPluginManager().registerEvents(new TownProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new ShopListener(shopManager, shopGUI, currencyManager), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(updateChecker), this);
        getServer().getPluginManager().registerEvents(new SellChestListener(shopManager, currencyManager), this);

        getLogger().info("KnightTowns has been enabled!");
    }

    @Override
    public void onDisable() {
        PlayerManager.saveAll();
        getLogger().info("KnightTowns has been disabled!");
    }

    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }
}
