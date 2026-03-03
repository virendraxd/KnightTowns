package com.knightgost.knighttowns.utils;

import com.knightgost.knighttowns.model.TownRank;
import com.knightgost.knighttowns.model.TownPermission;

public class TownPerms {

    public static boolean hasPermission(TownRank rank, TownPermission perm) {
        return switch (rank) {
            case TOWNMASTER -> true; // Full power
            case MAYOR -> switch (perm) {
                case ADD_MEMBER, REMOVE_MEMBER, CLAIM, UNCLAIM -> true;
                default -> false;
            };
            case ASSISTANT -> switch (perm) {
                case ADD_MEMBER -> true;
                default -> false;
            };
            default -> false;
        };
    }
}
