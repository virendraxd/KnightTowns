package com.knightgost.knighttowns.listeners;

import com.knightgost.knighttowns.gui.TownGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class TownGUIListener implements Listener {

    public TownGUIListener(TownGUI townGUI) {
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().title().equals(Component.text("§6§lKnightTowns"))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (e.getPlayer().getOpenInventory().title().equals(Component.text("§6§lKnightTowns"))) {
            e.setCancelled(true);
        }
    }
}
