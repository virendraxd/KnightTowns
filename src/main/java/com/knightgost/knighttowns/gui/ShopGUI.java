package com.knightgost.knighttowns.gui;

import com.knightgost.knighttowns.manager.CurrencyManager;
import com.knightgost.knighttowns.manager.ShopManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopGUI {

    private final JavaPlugin plugin;
    private final ShopManager manager;
    private final CurrencyManager currencyManager;

    public ShopGUI(JavaPlugin plugin, ShopManager manager, CurrencyManager currencyManager) {
        this.plugin = plugin;
        this.manager = manager;
        this.currencyManager = currencyManager;
    }

    /** MAIN SHOP MENU (Categories) */
    public void open(Player player) {
        String title = manager.getConfig().getString("shop.title", "Shop"); // from shop.yml
        Inventory gui = Bukkit.createInventory(null, 36, Component.text(title));

        for (String category : manager.getCategories()) {

            Material material = manager.getCategoryIcon(category);
            if (material == null)
                material = Material.BARRIER;

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();

            meta.displayName(Component.text(manager.getCategoryName(category)));

            List<Component> lore = new ArrayList<>();
            for (String line : manager.getCategoryLore(category)) {
                lore.add(Component.text(line));
            }
            meta.lore(lore);

            item.setItemMeta(meta);

            gui.setItem(manager.getCategorySlot(category), item);
        }

        player.openInventory(gui);
    }

    /** CATEGORY ITEMS VIEWER (default page 0) */
    public void openCategory(Player player, String category) {
        openCategory(player, category, 0);
    }

    /**
     * Paginated category viewer.
     * page: 0-based page index
     */
    public void openCategory(Player player, String category, int page) {

        // prepare title: strip §f from config name then apply §8
        String rawName = manager.getCategoryName(category).replace("§f", "").replace("§", "");
        String title = "§8" + rawName;

        Inventory gui = Bukkit.createInventory(null, 45, Component.text(title));

        // usable center slots (19 total)
        int[] usableSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        List<Map<String, Object>> items = manager.getItems(category);
        if (items == null)
            items = new ArrayList<>();

        final int perPage = usableSlots.length;
        final int totalItems = items.size();
        final int lastPage = Math.max(0, (int) Math.ceil((double) totalItems / perPage) - 1);

        // clamp page
        if (page < 0)
            page = 0;
        if (page > lastPage)
            page = lastPage;

        // start index in items for this page
        int startIndex = page * perPage;
        int endIndex = Math.min(startIndex + perPage, totalItems);

        // place items for this page
        int slotIdx = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Map<String, Object> map = items.get(i);

            Material mat = Material.matchMaterial((String) map.get("material"));
            if (mat == null)
                continue;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();

            // Force white colored name, strip any existing § codes
            String name = ((String) map.get("name")).replace("§", "");
            meta.displayName(Component.text("§f" + name));

            int buy = ((Number) map.get("buy")).intValue();

            // New clean lore
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("")); // blank line
            lore.add(Component.text("§7Cost"));
            lore.add(Component.text("§6" + buy + " Coins"));
            lore.add(Component.text(""));
            lore.add(Component.text("§eClick to buy!"));
            lore.add(Component.text("§eRight-click for more trading options!"));

            meta.lore(lore);
            item.setItemMeta(meta);

            gui.setItem(usableSlots[slotIdx++], item);
        }

        // NAVIGATION: Previous (slot 18), Next (slot 26), Back (slot 36)
        // Previous (only if page > 0)
        if (page > 0) {
            gui.setItem(18, pageButton(Material.ARROW, "§e◀ Previous Page", "prev", page - 1));
        } else {
            gui.setItem(18, fillerPane()); // optional filler so slot unusable
        }

        // Next (only if page < lastPage)
        if (page < lastPage) {
            gui.setItem(26, pageButton(Material.ARROW, "§eNext Page ▶", "next", page + 1));
        } else {
            gui.setItem(26, fillerPane());
        }

        // Back (always visible) at bottom-left (slot 36)
        gui.setItem(36, backButton());

        player.openInventory(gui);

        // store metadata to know which category + page this player is viewing
        player.setMetadata("shop_view_category", new FixedMetadataValue(plugin, category));
        player.setMetadata("shop_view_page", new FixedMetadataValue(plugin, page));
    }

    /*
     * Helper: create a page navigation button (stores page index in lore for
     * listener)
     */
    private ItemStack pageButton(Material mat, String name, String key, int pageIndex) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        // store page index in lore as plain number so listener can parse it
        meta.lore(List.of(Component.text(String.valueOf(pageIndex))));
        item.setItemMeta(meta);
        return item;
    }

    /* Helper: back button */
    private ItemStack backButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§cBack"));
        meta.lore(List.of(Component.text("§7Return to categories")));
        item.setItemMeta(meta);
        return item;
    }

    /* optional filler pane so border slots cannot be used */
    private ItemStack fillerPane() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    /* Amount selector methods unchanged (kept for completeness) */

    public void openAmountSelector(Player player, ItemStack originalItem, int basePrice) {

        int amount = 1; // default
        player.setMetadata("shop_amount", new FixedMetadataValue(plugin, amount));
        player.setMetadata("shop_item", new FixedMetadataValue(plugin, originalItem.getType().toString()));
        player.setMetadata("shop_price", new FixedMetadataValue(plugin, basePrice));

        openAmountSelectorWithAmount(player, amount,
                originalItem.getType(), basePrice);
    }

    private void openAmountSelectorWithAmount(Player player, int amount, Material mat, int basePrice) {

        Inventory gui = Bukkit.createInventory(null, 27,
                Component.text("Purchase Confirmation"));

        // ========= DYNAMIC REMOVE BUTTONS =========
        if (amount >= 65)
            gui.setItem(10, amountButton(Material.RED_STAINED_GLASS_PANE, "-64", -64));

        if (amount >= 11)
            gui.setItem(11, amountButton(Material.RED_STAINED_GLASS_PANE, "-10", -10));

        if (amount > 1)
            gui.setItem(12, amountButton(Material.RED_STAINED_GLASS_PANE, "-1", -1));

        // ========= CENTER ITEM =========
        gui.setItem(13, itemDisplay(mat, amount));

        // ========= ALWAYS SHOW ADD BUTTONS =========
        gui.setItem(14, amountButton(Material.LIME_STAINED_GLASS_PANE, "+1", 1));
        gui.setItem(15, amountButton(Material.LIME_STAINED_GLASS_PANE, "+10", 10));
        gui.setItem(16, amountButton(Material.LIME_STAINED_GLASS_PANE, "+64", 64));

        // ========= CONFIRM & CANCEL =========
        gui.setItem(22, confirmButton());
        gui.setItem(23, cancelButton());

        player.openInventory(gui);
    }

    public void updateAmountAndRefresh(Player player, int newAmount) {

        if (newAmount < 1)
            newAmount = 1; // NEVER below 1
        if (newAmount > 2304)
            newAmount = 2304; // 36 stacks cap

        player.setMetadata("shop_amount", new FixedMetadataValue(plugin, newAmount));

        if (!player.hasMetadata("shop_item") || !player.hasMetadata("shop_price"))
            return;

        String matName = player.getMetadata("shop_item").get(0).asString();
        int basePrice = player.getMetadata("shop_price").get(0).asInt();

        Material mat = Material.matchMaterial(matName);
        if (mat == null)
            return;

        openAmountSelectorWithAmount(player, newAmount, mat, basePrice);
    }

    private ItemStack amountButton(Material mat, String name, int value) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));

        meta.lore(List.of(Component.text(String.valueOf(value)))); // store -1, +10 etc.
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack itemDisplay(Material type, int amount) {
        ItemStack item = new ItemStack(type);

        item.setAmount(Math.min(amount, item.getMaxStackSize()));

        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§eAmount: §6" + amount));
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack confirmButton() {
        ItemStack item = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§aConfirm Purchase"));
        meta.lore(List.of(Component.text("§7Click to confirm")));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack cancelButton() {
        ItemStack item = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§cCancel"));
        meta.lore(List.of(Component.text("§7Close without buying")));
        item.setItemMeta(meta);
        return item;
    }

    public ShopManager getManager() {
        return manager;
    }

    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }
}
