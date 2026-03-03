package com.knightgost.knighttowns.gui;

import com.knightgost.knighttowns.manager.CurrencyManager;
import com.knightgost.knighttowns.manager.PlayerXPManager;
import com.knightgost.knighttowns.model.Town;
import com.knightgost.knighttowns.manager.TownManager;
import com.knightgost.knighttowns.model.TownRank;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer; // Necessary import
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Collectors; // Necessary import
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class TownGUI {
        private final CurrencyManager currencyManager;

        public TownGUI(JavaPlugin plugin, CurrencyManager currencyManager) {
                this.currencyManager = currencyManager;
        }

        // ==================================================
        // MAIN GUI OPEN METHOD
        // ==================================================
        public void open(Player player) {

                // Build placeholders ONCE (single source of truth)
                Map<String, String> placeholders = buildPlaceholders(player);

                Inventory gui = Bukkit.createInventory(
                                player,
                                45,
                                Component.text("§6§lKnightTowns"));

                // ─── Player Head (slot 4) ───
                gui.setItem(4, createPlayerHead(player, placeholders));

                // ─── Town & Info Icons ───
                gui.setItem(20, createIcon(
                                Material.PAPER,
                                "§b§lYour Town",
                                placeholders,
                                "§8⎯⎯⎯⎯ Town Overview ⎯⎯⎯⎯",
                                "§7Name: §b{town_name}"));

                gui.setItem(21, createIcon(
                                Material.GRAY_DYE,
                                "§8Soon",
                                Map.of(),
                                "§7Feature coming soon"));

                gui.setItem(22, createIcon(
                                Material.GRAY_DYE,
                                "§8Soon",
                                Map.of(),
                                "§7Feature coming soon"));

                gui.setItem(23, createIcon(
                                Material.GRAY_DYE,
                                "§8Soon",
                                Map.of(),
                                "§7Feature coming soon"));

                gui.setItem(24, createIcon(
                                Material.GRAY_DYE,
                                "§8Soon",
                                Map.of(),
                                "§7Feature coming soon"));

                fillEmpty(gui);
                player.openInventory(gui);
        }

        // ==================================================
        // PLACEHOLDERS
        // ==================================================
        private Map<String, String> buildPlaceholders(Player player) {

                Map<String, String> map = new HashMap<>();

                // ─── Player Data ───
                map.put("coins", currencyManager.getBalance(player).toPlainString());
                map.put("xp", String.valueOf(PlayerXPManager.getXP(player.getUniqueId())));
                map.put("level", String.valueOf(PlayerXPManager.getLevel(player.getUniqueId())));
                map.put("rank", getPlayerTownRank(player));

                // ─── Town Data ───
                Town town = TownManager.getTownByPlayer(player.getUniqueId());

                map.put("town_name", town != null ? town.getName() : "No Town");
                map.put("town_level", town != null
                                ? String.valueOf(TownManager.getTownLevel(town))
                                : "0");

                map.put("population", town != null
                                ? String.valueOf(town.getMembers().size())
                                : "0");

                // ─── Extra / Future ───
                map.put("shards", "50");
                map.put("power", "100");
                map.put("next_level_xp", "500");
                map.put("boost", "10%");
                map.put("daily_earn", "50");
                map.put("total_earn", "1500");

                map.put("sound_status", "ON");
                map.put("particle_status", "OFF");
                map.put("notify_status", "ON");
                map.put("chat_mode", "Compact");

                return map;
        }

        // ==================================================
        // GUI BUILDERS
        // ==================================================
        private ItemStack createPlayerHead(Player player, Map<String, String> placeholders) {

                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();

                meta.setOwningPlayer(player);
                meta.displayName(Component.text("§e" + player.getName()));

                meta.lore(replacePlaceholders(
                                List.of(
                                                Component.text("§8⎯⎯⎯⎯⎯ Player Info ⎯⎯⎯⎯⎯"),
                                                Component.text("§7Rank: §b{rank}"),
                                                Component.text("§7Level: §a{level}"),
                                                Component.text("§7XP: §e{xp}"),
                                                Component.text("§7Coins: §6{coins}")),
                                placeholders));

                head.setItemMeta(meta);
                return head;
        }

        private ItemStack createIcon(
                        Material material,
                        String name,
                        Map<String, String> placeholders,
                        String... loreLines) {
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();

                meta.displayName(Component.text(name));

                List<Component> lore = new ArrayList<>();
                for (String line : loreLines) {
                        lore.add(Component.text(line));
                }

                meta.lore(replacePlaceholders(lore, placeholders));
                item.setItemMeta(meta);
                return item;
        }

        // ==================================================
        // UTILITIES
        // ==================================================
        private List<Component> replacePlaceholders(
                        List<Component> lore,
                        Map<String, String> placeholders) {
                return lore.stream()
                                .map(component -> {
                                        String text = LegacyComponentSerializer
                                                        .legacySection()
                                                        .serialize(component);

                                        for (Map.Entry<String, String> e : placeholders.entrySet()) {
                                                text = text.replace("{" + e.getKey() + "}", e.getValue());
                                        }

                                        return Component.text(text);
                                })
                                .collect(Collectors.toList());
        }

        private void fillEmpty(Inventory inv) {
                ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta meta = filler.getItemMeta();
                meta.displayName(Component.text(" "));
                filler.setItemMeta(meta);

                for (int i = 0; i < inv.getSize(); i++) {
                        if (inv.getItem(i) == null)
                                inv.setItem(i, filler);
                }
        }

        // ==================================================
        // TOWN RANK
        // ==================================================
        private String getPlayerTownRank(Player player) {

                Town town = TownManager.getTownByPlayer(player.getUniqueId());
                if (town == null)
                        return "Visitor";

                TownRank rank = town.getRank(player.getUniqueId());
                String name = rank.name();

                return name.substring(0, 1) + name.substring(1).toLowerCase();
        }
}
