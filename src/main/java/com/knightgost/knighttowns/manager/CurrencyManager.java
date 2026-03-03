package com.knightgost.knighttowns.manager;

import com.knightgost.knighttowns.model.PlayerData;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class CurrencyManager {

    public BigDecimal getBalance(Player player) {
        return PlayerManager
                .getData(player.getUniqueId())
                .getBalance();
    }

    public void setBalance(Player player, BigDecimal amount) {
        PlayerData data = PlayerManager.getData(player.getUniqueId());
        data.setBalance(amount);
        PlayerManager.savePlayer(player.getUniqueId());
    }

    public void addBalance(Player player, BigDecimal amount) {
        PlayerData data = PlayerManager.getData(player.getUniqueId());
        data.addBalance(amount);
        PlayerManager.savePlayer(player.getUniqueId());
    }

    public boolean removeBalance(Player player, BigDecimal amount) {
        PlayerData data = PlayerManager.getData(player.getUniqueId());

        if (data.getBalance().compareTo(amount) < 0) {
            return false;
        }

        data.removeBalance(amount);
        PlayerManager.savePlayer(player.getUniqueId());
        return true;
    }

    public boolean hasEnough(Player player, BigDecimal price) {
        return getBalance(player).compareTo(price) >= 0;
    }
}
