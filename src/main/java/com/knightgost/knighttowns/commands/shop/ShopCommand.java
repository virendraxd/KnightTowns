//package com.knightgost.knighttowns.commands.shop;
//
//import com.knightgost.knighttowns.gui.ShopGUI;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//
//public class ShopCommand implements CommandExecutor {
//
//    // STATIC entry point so TownCommand can call it
//    public static void openShop(Player player) {
//        if (player == null) return;
//
//        // Open your actual GUI
//        shopGUI.open(player);
//    }
//
//
//    // Optional: /shop command (if you register it)
//    @Override
//    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//
//        if (!(sender instanceof Player player)) {
//            sender.sendMessage("Only players can use this.");
//            return true;
//        }
//
//        // No subcommands for now → simply open shop
//        openShop(player);
//        return true;
//    }
//}
