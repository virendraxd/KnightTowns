package com.knightgost.knighttowns.commands.currency;

import com.knightgost.knighttowns.manager.CurrencyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class CurrencyAdminHandler {

    private final CurrencyManager currencyManager;

    public CurrencyAdminHandler(CurrencyManager currencyManager) {
        this.currencyManager = currencyManager;
    }

    public void handleSetMoney(Player admin, String targetName, String amountStr) {

        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            admin.sendMessage("§cPlayer not found!");
            return;
        }

        BigDecimal amount = new BigDecimal(amountStr);
        currencyManager.setBalance(target, amount);

        admin.sendMessage("§aSet §e" + target.getName() + "§a's balance to §e" + amount);
        target.sendMessage("§6Your balance was updated by an admin.");
    }
}
