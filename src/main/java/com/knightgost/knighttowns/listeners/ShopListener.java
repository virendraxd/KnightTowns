package com.knightgost.knighttowns.listeners;

import com.knightgost.knighttowns.gui.ShopGUI;
import com.knightgost.knighttowns.manager.CurrencyManager;
import com.knightgost.knighttowns.manager.ShopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ShopListener implements Listener {

    private final ShopManager manager;
    private final ShopGUI gui;
    private final CurrencyManager currencyManager;

    public ShopListener(ShopManager manager, ShopGUI gui, CurrencyManager currencyManager) {
        this.manager = manager;
        this.gui = gui;
        this.currencyManager = currencyManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player player))
            return;

        Component titleComp = e.getView().title();

        // ------------------------------------------------
        // 1) MAIN SHOP (CATEGORY ICONS)
        // ------------------------------------------------
        if (titleComp.equals(Component.text(manager.getConfig().getString("shop.title", "Shop")))) {

            e.setCancelled(true); // Cancel ALL clicks in main shop

            if (e.getClickedInventory() != e.getView().getTopInventory())
                return;
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR)
                return;

            String clickedName = PlainTextComponentSerializer.plainText()
                    .serialize(clicked.getItemMeta().displayName());

            for (String category : manager.getCategories()) {
                if (clickedName.equals(manager.getCategoryName(category))) {
                    gui.openCategory(player, category);
                    return;
                }
            }
            player.sendMessage("Clicked: " + e.getClick() + " on " + clicked.getType());

            return;
        }

        // ------------------------------------------------
        // 2) CATEGORY CLICK HANDLING + PAGE BUTTONS
        // ------------------------------------------------
        if (PlainTextComponentSerializer.plainText().serialize(titleComp).startsWith("§8")) {
            e.setCancelled(true);

            if (e.getClickedInventory() != e.getView().getTopInventory())
                return;
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR)
                return;

            String categoryName = PlainTextComponentSerializer.plainText().serialize(titleComp).substring(2); // remove
                                                                                                              // §8
            String categoryKey = null;

            for (String key : manager.getCategories()) {
                if (manager.getCategoryName(key).replace("§f", "").equals(categoryName)) {
                    categoryKey = key;
                    break;
                }
            }
            if (categoryKey == null)
                return;

            int page = 0;
            if (player.hasMetadata("shop_view_page")) {
                page = player.getMetadata("shop_view_page").get(0).asInt();
            }

            // -------------------------------
            // PAGE BUTTONS
            // -------------------------------
            if (e.getSlot() == 26) { // NEXT PAGE
                gui.openCategory(player, categoryKey, page + 1);
                return;
            }

            if (e.getSlot() == 18) { // PREVIOUS PAGE
                if (page > 0)
                    gui.openCategory(player, categoryKey, page - 1);
                return;
            }

            if (e.getSlot() == 36) { // Back Button
                gui.open(player);
                return;
            }
            // -------------------------------
            // BACK TO CATEGORIES
            // -------------------------------
            if (clicked.getType() == Material.ARROW &&
                    clicked.getItemMeta().displayName().equals(Component.text("§fBack"))) {
                gui.open(player);
                return;
            }

            // -------------------------------
            // NORMAL ITEM CLICK
            // -------------------------------
            List<Map<String, Object>> items = manager.getItems(categoryKey);
            if (items == null)
                return;

            Material clickedMaterial = clicked.getType();

            for (Map<String, Object> map : items) {
                Material mat = Material.matchMaterial(map.get("material").toString());
                if (mat == null)
                    continue;

                int buy = ((Number) map.get("buy")).intValue();

                if (clickedMaterial != mat)
                    continue;

                String displayName = map.get("name") != null
                        ? map.get("name").toString()
                        : capitalize(mat.name().replace("_", " "));

                // BUY 1 (left click)
                if (e.getClick().isLeftClick()) {
                    BigDecimal price = BigDecimal.valueOf(buy);

                    if (!currencyManager.hasEnough(player, price)) {
                        player.sendMessage("§cYou don't have enough Coins.");
                        return;
                    }

                    currencyManager.removeBalance(player, price);
                    player.getInventory().addItem(new ItemStack(mat, 1));

                    player.sendMessage("§aYou bought §f" + displayName + " §afor §6" + price + " Coins§a.");
                    return;
                }

                // OPEN AMOUNT SELECTOR (right click)
                if (e.getClick().isRightClick()) {
                    gui.openAmountSelector(player, new ItemStack(mat), buy);
                    return;
                }
            }
        }

        // ------------------------------------------------
        // 3) AMOUNT SELECTOR if (manager.getCategoryName(key).equals(categoryName))
        // ------------------------------------------------
        if (titleComp.equals(Component.text("Purchase Confirmation"))) {
            e.setCancelled(true);

            if (e.getClickedInventory() == null)
                return;
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR)
                return;

            if (!player.hasMetadata("shop_amount"))
                return;

            int amount = player.getMetadata("shop_amount").get(0).asInt();
            String matName = player.getMetadata("shop_item").get(0).asString();
            int basePrice = player.getMetadata("shop_price").get(0).asInt();

            Material mat = Material.matchMaterial(matName);
            if (mat == null)
                return;

            // Pretty name
            String prettyName = capitalize(mat.name().replace("_", " "));

            // CANCEL
            if (clicked.getType() == Material.RED_CONCRETE) {
                player.closeInventory();
                return;
            }

            // CONFIRM
            if (clicked.getType() == Material.LIME_CONCRETE) {

                BigDecimal totalCost = BigDecimal.valueOf(basePrice)
                        .multiply(BigDecimal.valueOf(amount));

                if (!currencyManager.hasEnough(player, totalCost)) {
                    player.sendMessage("§cYou don't have enough Coins.");
                    player.closeInventory();
                    return;
                }

                currencyManager.removeBalance(player, totalCost);
                player.getInventory().addItem(new ItemStack(mat, amount));

                player.sendMessage(
                        "§aYou bought §f" + prettyName +
                                " §8x§e" + amount +
                                " §afor §6" + totalCost.toPlainString() + " Coins§a.");

                player.closeInventory();
            }

            // +/- BUTTONS
            if (clicked.getItemMeta().lore() != null) {
                String raw = PlainTextComponentSerializer.plainText()
                        .serialize(clicked.getItemMeta().lore().get(0));

                try {
                    int change = Integer.parseInt(raw.trim());
                    gui.updateAmountAndRefresh(player, amount + change);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return str;
        String[] words = str.toLowerCase().split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty())
                continue;
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}
