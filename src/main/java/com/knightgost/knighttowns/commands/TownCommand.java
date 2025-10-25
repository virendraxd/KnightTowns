package com.knightgost.knighttowns.commands;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.knightgost.knighttowns.KnightTowns;
import com.knightgost.knighttowns.data.Town;
import com.knightgost.knighttowns.data.TownManager;
import com.knightgost.knighttowns.data.TownRank;
import com.knightgost.knighttowns.utils.BorderUtils;

public class TownCommand implements CommandExecutor {

    private final KnightTowns plugin;
    private final Map<String, Town> towns = new HashMap<>();

    public TownCommand(KnightTowns plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help" -> handleHelp(player);
            case "create" -> handleCreate(player, args);
            case "delete" -> handleDelete(player, args);
            case "list" -> handleList(player);
            case "showborder" -> handleShowBorder(player, args);
            case "claim" -> handleClaim(player);
            // case "togglebossbar" ->  handleShowBossBar(player, args);
            case "addmember" ->  handleAddMember(player, args);
            case "removemember" ->  handleRemoveMember(player, args);
            case "setrank" -> handleSetRank(player, args);
            default -> sendUsage(player);
        }
        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage("§eUsage: /town help §7- Show list of commands");
    }
    
    private void handleHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== Town Commands ===");
        sender.sendMessage("§e/town create <name> §7- Create a new town (TownMaster only)");
        sender.sendMessage("§e/town list §7- List all towns with Mayor info");
        sender.sendMessage("§e/town delete <name> §7- Delete a town (TownMaster only)");
        sender.sendMessage("§e/town showborder §7- Show the border of your town for a few seconds");
        sender.sendMessage("§e/town claim §7- Claim the chunk you are standing on for your town");
        sender.sendMessage("§e/town addmember <player> <rank> §7- Add a player to your town (Mayor/TownMaster only)");
        sender.sendMessage("§e/town removemember <player> §7- Remove a player from your town (Mayor/TownMaster only)");
        // sender.sendMessage("§e/town togglebossbar §7- Toggle the town border bossbar display");
    }

    /**
     * /town create <name> — creates a town in the chunk the player stands on.
     */
    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§eUsage: /town create <name>");
            return;
        }

        // ✅ Only OPs or players with permission can create towns
        if (!player.isOp()) {
            player.sendMessage("§cOnly OPs can create a town!");
            return;
        }

        String townName = args[1].toLowerCase();

        // Check memory first
        if (towns.containsKey(townName)) {
            player.sendMessage("§cA town with that name already exists!");
            return;
        }

        // Check YAML file to prevent duplicates after reload
        File file = new File(plugin.getDataFolder(), "towns.yml");
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            if (config.contains(townName)) {
                player.sendMessage("§cA town with that name already exists in file!");
                return;
            }
        }

        // Create new town
        Town town = new Town(townName, player.getUniqueId());
        town.addMember(player.getUniqueId(), TownRank.TOWNMASTER);

        // Claim the chunk where the player is standing
        Chunk currentChunk = player.getLocation().getChunk();
        town.addClaimedChunk(currentChunk);

        // Save to memory
        towns.put(townName, town);
        TownManager.addTown(town);

        // Save to file
        TownManager.saveTownToFile(plugin, town);

        player.sendMessage("§a✅ Town §6" + townName + "§a created and claimed your current chunk!");

        // Show border immediately
        BorderUtils.showTownBorder(plugin, player, town, 30); // 30 seconds
    }


    /**
     * /town delete <name> — deletes a town if the player is the mayor.
     */
        private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§eUsage: /town delete <name>");
            return;
        }

        String townName = args[1].toLowerCase();
        
        File file = new File(plugin.getDataFolder(), "towns.yml");
        if (!file.exists()) {
            player.sendMessage("§cNo towns exist to delete.");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.contains(townName)) {
            player.sendMessage("§cThat town does not exist.");
            return;
        }
        
        Town town = TownManager.getTown(townName);
        if (town == null) {
            player.sendMessage("§cTown data is missing in memory!");
            return;
        }
        
        // ✅ Only TownMasters or OPs can delete towns
        TownRank playerRank = town.getRank(player.getUniqueId());
        if (playerRank != TownRank.TOWNMASTER && !player.isOp()) {
            player.sendMessage("§cOnly the TownMaster or an OP can delete a town!");
            return;
        }

        // Remove town from file
        config.set(townName, null);
        try {
            config.save(file);
            
            // Remove from memory
            TownManager.removeTown(townName);
            
            player.sendMessage("§a🏙 Town '" + townName + "' has been deleted.");
        } catch (IOException e) {
            player.sendMessage("§cFailed to delete town. Check console for details.");
            e.printStackTrace();
        }
    }


    /**
     * /town list — shows all towns.
     */
private void handleList(CommandSender sender) {
    File file = new File(plugin.getDataFolder(), "towns.yml");
    if (!file.exists()) {
        sender.sendMessage("§cNo towns have been created yet!");
        return;
    }

    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
    Set<String> townNames = config.getKeys(false);

    if (townNames.isEmpty()) {
        sender.sendMessage("§7There are currently no towns created.");
        return;
    }

    sender.sendMessage("§6🏙 Towns List:");

    for (String townName : townNames) {
        String mayorName = "Unknown";

        // Get the town object from memory
        Town town = TownManager.getTown(townName);
        if (town != null) {
            // Find the player with rank MAYOR
            for (Map.Entry<UUID, TownRank> entry : town.getMembers().entrySet()) {
                if (entry.getValue() == TownRank.MAYOR) {
                    OfflinePlayer mayor = Bukkit.getOfflinePlayer(entry.getKey());
                    if (mayor != null && mayor.getName() != null) {
                        mayorName = mayor.getName();
                    } else {
                        mayorName = entry.getKey().toString().substring(0, 8);
                    }
                    break; // stop after first mayor found
                }
            }
        }

        sender.sendMessage("§e- §a" + townName + " §7(§6Mayor:§f " + mayorName + "§7)");
    }
}


private void handleShowBorder(Player player, String[] args) {
    if (args.length < 2) {
            player.sendMessage("§eUsage: /town showborder <town>");
            return;
        }

        String townName = args[1].toLowerCase();
        Town town = TownManager.getTown(townName);

        if (town == null) {
            player.sendMessage("§c⚠ Town '" + townName + "' not found!");
            return;
        }

        if (town.getClaimedChunks().isEmpty()) {
            player.sendMessage("§cThis town has no claimed chunks!");
            return;
        }

        // Show only the outer border of the town
        BorderUtils.showTownBorder(plugin, player, town, 30);

        player.sendMessage("§a🏙 Showing borders for town §e" + town.getName() + " §a!");
    }

    // Claim command handler
private void handleClaim(Player player) {
    UUID playerUUID = player.getUniqueId();

    // Get player's town
    Town town = TownManager.getPlayerTown(playerUUID);
    if (town == null) {
        player.sendMessage("§cYou are not part of any town!");
        return;
    }

    // Check if player is the mayor
    TownRank rank = town.getRank(playerUUID);
    if (rank != TownRank.MAYOR) {
        player.sendMessage("§cOnly the mayor can claim new chunks!");
        return;
    }

    // Get current chunk
    Chunk currentChunk = player.getLocation().getChunk();
    String chunkKey = currentChunk.getWorld().getName() + "," + currentChunk.getX() + "," + currentChunk.getZ();

    // Check if already claimed
    if (town.getClaimedChunks().contains(chunkKey)) {
        player.sendMessage("§eThis chunk is already claimed by your town!");
        return;
    }

    // Check if another town has already claimed it
    Town existingOwner = TownManager.getTownByChunk(currentChunk);
    if (existingOwner != null) {
        player.sendMessage("§cThis chunk is already claimed by another town: §e" + existingOwner.getName());
        return;
    }

    // Claim the chunk
    town.addClaimedChunk(currentChunk);

    // Save the town data
    try {
        TownManager.saveTownToFile(plugin, town);
    } catch (Exception e) {
        player.sendMessage("§cFailed to save town data! Contact an admin.");
        e.printStackTrace();
        return;
    }

    player.sendMessage("§a✅ You have claimed this chunk for your town!");

    // Optional: show visual border for 30 seconds
    BorderUtils.showTownBorder(plugin, player, town, 30);
}


    // private void handleShowBossBar(Player player, String[] args) {
    //     if (args.length < 2) {
    //         player.sendMessage("§eUsage: /town showbossbar <town>");
    //         return;
    //     }

    //     String townName = args[1].toLowerCase();
    //     Town town = TownManager.getTown(townName);

    //     if (town == null) {
    //         player.sendMessage("§c⚠ Town '" + townName + "' not found!");
    //         return;
    //     }

    //     // Show or update bossbar
    //     plugin.bossBarManager.showOrUpdateBossBar(player, town.getName());

    //     player.sendMessage("§a🏙 Showing bossbar for town §e" + town.getName() + " §a!");
    // }

    private void handleAddMember(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§eUsage: /town addmember <player> <rank>");
            return;
        }

        Town town = TownManager.getTownByPlayer(player.getUniqueId());
        if (town == null) {
            player.sendMessage("§cYou are not a member of any town.");
            return;
        }

        TownRank playerRank = town.getRank(player.getUniqueId());
        if (playerRank == null) {
            player.sendMessage("§cYou have no rank in this town!");
            return;
        }

        String targetName = args[1];
        OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
        if (offline == null || offline.getUniqueId() == null) {
            player.sendMessage("§cCould not find that player (try exact name).");
            return;
        }
        UUID targetUUID = offline.getUniqueId();

        // Prevent adding existing member
        if (town.isMember(targetUUID)) {
            player.sendMessage("§eThat player is already a member of your town!");
            return;
        }

        // Parse rank
        TownRank rank;
        try {
            rank = TownRank.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid rank. Use: TOWNMASTER, MAYOR, ASSISTANT, MEMBER, VISITOR");
            return;
        }

        // Permission rules
        if (playerRank == TownRank.TOWNMASTER) {
            // full access
        } else if (playerRank == TownRank.MAYOR) {
            if (rank == TownRank.TOWNMASTER || rank == TownRank.MAYOR) {
                player.sendMessage("§cYou cannot add someone as TownMaster or Mayor!");
                return;
            }
        } else {
            player.sendMessage("§cYou do not have permission to add members!");
            return;
        }

        // Add member
        town.addMember(targetUUID, rank);
        TownManager.saveTownToFile(plugin, town);
        TownManager.addTown(town);

        player.sendMessage("§aAdded §e" + offline.getName() + " §ato town as §b" + rank.name());
        if (offline.isOnline()) {
            ((Player) offline).sendMessage("§aYou have been added to §e" + town.getName() + " §aas §b" + rank.name());
        }
    }

private void handleSetRank(Player player, String[] args) {
    if (args.length < 3) {
        player.sendMessage("§eUsage: /town setrank <player> <rank>");
        return;
    }

    Town town = TownManager.getTownByPlayer(player.getUniqueId());
    if (town == null) {
        player.sendMessage("§cYou are not a member of any town.");
        return;
    }

    TownRank playerRank = town.getRank(player.getUniqueId());
    if (playerRank == null) {
        player.sendMessage("§cYou have no rank in this town!");
        return;
    }

    String targetName = args[1];
    OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
    if (offline == null || offline.getUniqueId() == null) {
        player.sendMessage("§cCould not find that player (try exact name).");
        return;
    }
    UUID targetUUID = offline.getUniqueId();

    if (!town.isMember(targetUUID)) {
        player.sendMessage("§cThat player is not a member of your town!");
        return;
    }

    // Parse new rank
    TownRank newRank;
    try {
        newRank = TownRank.valueOf(args[2].toUpperCase());
    } catch (IllegalArgumentException e) {
        player.sendMessage("§cInvalid rank. Use: TOWNMASTER, MAYOR, ASSISTANT, MEMBER, VISITOR");
        return;
    }

    TownRank targetRank = town.getRank(targetUUID);

    // Permission rules
    if (playerRank == TownRank.TOWNMASTER) {
        // Can change anyone's rank
    } else if (playerRank == TownRank.MAYOR) {
        // Cannot touch TownMaster or other Mayors
        if (targetRank == TownRank.TOWNMASTER || targetRank == TownRank.MAYOR) {
            player.sendMessage("§cYou cannot change the rank of a TownMaster or Mayor!");
            return;
        }
        if (newRank == TownRank.TOWNMASTER || newRank == TownRank.MAYOR) {
            player.sendMessage("§cYou cannot promote someone to Mayor or TownMaster!");
            return;
        }
    } else {
        player.sendMessage("§cYou do not have permission to set ranks!");
        return;
    }

    town.addMember(targetUUID, newRank);
    TownManager.saveTownToFile(plugin, town);
    TownManager.addTown(town);

    player.sendMessage("§aChanged §e" + offline.getName() + "§a's rank to §b" + newRank.name());
    if (offline.isOnline()) {
        ((Player) offline).sendMessage("§aYour rank in §e" + town.getName() + " §ais now §b" + newRank.name());
    }
}

private void handleRemoveMember(Player player, String[] args) {
    if (args.length < 2) {
        player.sendMessage("§eUsage: /town removemember <player>");
        return;
    }

    Town town = TownManager.getTownByPlayer(player.getUniqueId());
    if (town == null) {
        player.sendMessage("§cYou are not a member of any town.");
        return;
    }

    TownRank playerRank = town.getRank(player.getUniqueId());
    if (playerRank == null) {
        player.sendMessage("§cYou have no rank in this town!");
        return;
    }

    String targetName = args[1];
    OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
    if (offline == null || offline.getUniqueId() == null) {
        player.sendMessage("§cCould not find that player (try exact name).");
        return;
    }
    UUID targetUUID = offline.getUniqueId();

    if (!town.isMember(targetUUID)) {
        player.sendMessage("§cThat player is not a member of your town!");
        return;
    }

    TownRank targetRank = town.getRank(targetUUID);

    // Permission rules
    if (playerRank == TownRank.TOWNMASTER) {
        // full access
    } else if (playerRank == TownRank.MAYOR) {
        if (targetRank == TownRank.TOWNMASTER || targetRank == TownRank.MAYOR) {
            player.sendMessage("§cYou cannot remove a TownMaster or another Mayor!");
            return;
        }
    } else {
        player.sendMessage("§cYou do not have permission to remove members!");
        return;
    }

    // Prevent self-removal unless OP
    if (player.getUniqueId().equals(targetUUID)) {
        player.sendMessage("§cYou cannot remove yourself from the town!");
        return;
    }

    // Remove the member
    town.removeMember(targetUUID);

    // Save and update memory
    TownManager.saveTownToFile(plugin, town);
    TownManager.addTown(town);

    player.sendMessage("§aRemoved §e" + offline.getName() + " §afrom your town.");
    if (offline.isOnline()) {
        ((Player) offline).sendMessage("§cYou have been removed from §e" + town.getName() + "§c.");
    }
}



}
