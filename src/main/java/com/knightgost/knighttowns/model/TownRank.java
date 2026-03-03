package com.knightgost.knighttowns.model;

public enum TownRank {
    TOWNMASTER,
    MAYOR,
    ASSISTANT,
    MEMBER,
    VISITOR;

    public boolean hasPermission(TownRank required) {
        return this.ordinal() <= required.ordinal();
    }
}
