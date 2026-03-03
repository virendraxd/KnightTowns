package com.knightgost.knighttowns.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SellChestGUI {

    public static final String TITLE = "§6Sell Items";
    public static final Component TITLE_COMPONENT = Component.text("§6Sell Items");

    public SellChestGUI() {
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(
                player,
                27,
                Component.text("§6Sell Items"));

        player.openInventory(inv);
    }
}
