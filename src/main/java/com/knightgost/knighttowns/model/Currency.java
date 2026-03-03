package com.knightgost.knighttowns.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Currency {

    private final UUID playerUUID;
    private BigDecimal balance;

    public Currency(UUID playerUUID, BigDecimal startingAmount) {
        this.playerUUID = playerUUID;
        this.balance = startingAmount;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal amount) {
        this.balance = amount;
    }

    public void add(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public boolean remove(BigDecimal amount) {
        if (this.balance.compareTo(amount) >= 0) {
            this.balance = this.balance.subtract(amount);
            return true;
        }
        return false;
    }

    public boolean hasEnough(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
}
