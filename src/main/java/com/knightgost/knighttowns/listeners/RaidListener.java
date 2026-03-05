package com.knightgost.knighttowns.listeners;

import com.knightgost.knighttowns.manager.RaidManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import java.util.UUID;

public class RaidListener implements Listener {
    private final RaidManager raidManager;

    public RaidListener(RaidManager raidManager) {
        this.raidManager = raidManager;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        raidManager.onMobKill(entity.getUniqueId());
    }

    @EventHandler
    public void onRaidDamage(EntityDamageByEntityEvent event) {
        // Track contributors
        if (event.getEntity() instanceof LivingEntity) {
            UUID mobUUID = event.getEntity().getUniqueId();
            if (raidManager.isRaidMob(mobUUID)) {
                if (event.getDamager() instanceof Player player) {
                    raidManager.addContributorIfRaidMob(mobUUID, player.getUniqueId());
                } else if (event.getDamager() instanceof org.bukkit.entity.Projectile proj) {
                    if (proj.getShooter() instanceof Player player) {
                        raidManager.addContributorIfRaidMob(mobUUID, player.getUniqueId());
                    }
                }
            }
        }

        // Prevent raid mobs from attacking non-town members if needed
        if (raidManager.isRaidMob(event.getDamager().getUniqueId())) {
            if (!(event.getEntity() instanceof Player)) {
                // Raid mobs focus on players
            }
        }
    }
}
