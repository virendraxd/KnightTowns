package com.knightgost.knighttowns.listeners;

import com.knightgost.knighttowns.gui.SellChestGUI;
import com.knightgost.knighttowns.manager.CurrencyManager;
import com.knightgost.knighttowns.manager.ShopManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Map;

public class SellChestListener implements Listener {

    private final ShopManager shopManager;
    private final CurrencyManager currencyManager;

    public SellChestListener(ShopManager shopManager, CurrencyManager currencyManager) {
        this.shopManager = shopManager;
        this.currencyManager = currencyManager;
    }

    @EventHandler
    public void onSellClose(InventoryCloseEvent e) {

        if (!(e.getPlayer() instanceof Player player))
            return;
        if (!e.getView().title().equals(SellChestGUI.TITLE_COMPONENT))
            return;

        Inventory inv = e.getInventory();

        BigDecimal totalEarnings = BigDecimal.ZERO;

        for (ItemStack item : inv.getContents()) {

            if (item == null || item.getType() == Material.AIR)
                continue;

            Material mat = item.getType();
            int amount = item.getAmount();

            Integer sellPrice = getSellPrice(mat);

            // ❌ Not sellable → return item
            if (sellPrice == null) {
                player.getInventory().addItem(item);
                continue;
            }

            // ✅ Sell item
            BigDecimal earned = BigDecimal.valueOf(sellPrice)
                    .multiply(BigDecimal.valueOf(amount));

            totalEarnings = totalEarnings.add(earned);
        }

        if (totalEarnings.compareTo(BigDecimal.ZERO) > 0) {
            currencyManager.addBalance(player, totalEarnings);
            player.sendMessage("§aYou sold items for §6" + totalEarnings + " Coins§a.");
        }
    }

    // -------------------------------
    // Find sell price from shop.yml
    // -------------------------------
    private Integer getSellPrice(Material mat) {

        for (String category : shopManager.getCategories()) {
            for (Map<String, Object> map : shopManager.getItems(category)) {

                if (!map.containsKey("sell"))
                    continue;

                Material shopMat = Material.matchMaterial(map.get("material").toString());

                if (shopMat == mat) {
                    return ((Number) map.get("sell")).intValue();
                }
            }
        }
        return null;
    }
}
