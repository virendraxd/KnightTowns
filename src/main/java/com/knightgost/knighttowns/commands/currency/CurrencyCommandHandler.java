package com.knightgost.knighttowns.commands.currency;

import com.knightgost.knighttowns.manager.CurrencyManager;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class CurrencyCommandHandler {

    private final CurrencyManager currencyManager;

    public CurrencyCommandHandler(CurrencyManager currencyManager) {
        this.currencyManager = currencyManager;
    }

    public void handleMoneyInfo(Player player) {
        player.sendMessage("§aYour Balance: §e" + currencyManager.getBalance(player));
    }

    public void handleMoneyAdd(Player player, String amountStr) {
        BigDecimal amount = new BigDecimal(amountStr);
        currencyManager.addBalance(player, amount);
        player.sendMessage("§aAdded §e" + amount + " §ato your balance.");
    }

    public void handleMoneyRemove(Player player, String amountStr) {
        BigDecimal amount = new BigDecimal(amountStr);

        if (!currencyManager.hasEnough(player, amount)) {
            player.sendMessage("§cYou don't have enough money!");
            return;
        }
        currencyManager.removeBalance(player, amount);
        player.sendMessage("§aMoney removed successfully!");
    }
}
