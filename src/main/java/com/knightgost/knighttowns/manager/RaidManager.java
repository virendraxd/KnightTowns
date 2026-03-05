package com.knightgost.knighttowns.manager;

import com.knightgost.knighttowns.KnightTowns;
import com.knightgost.knighttowns.model.RaidDifficulty;
import com.knightgost.knighttowns.model.Town;
import com.knightgost.knighttowns.model.TownRaid;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RaidManager {
    private final KnightTowns plugin;
    private final Map<String, TownRaid> activeRaids = new HashMap<>();
    private final Map<String, BossBar> raidBars = new HashMap<>();
    private final Random random = new Random();

    public RaidManager(KnightTowns plugin) {
        this.plugin = plugin;
        startRandomRaidTask();
    }

    private void startRandomRaidTask() {
        if (!plugin.getConfig().getBoolean("raids.enabled", true)) {
            return;
        }

        // Interval from config (default 2 hours = 144000 ticks)
        long intervalTicks = plugin.getConfig().getLong("raids.interval_ticks", 144000L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (TownManager.getAllTowns().isEmpty())
                    return;

                // Pick a random town
                List<Town> towns = new ArrayList<>(TownManager.getAllTowns());
                Town targetTown = towns.get(random.nextInt(towns.size()));

                // Ensure the town has claimed chunks and members online
                if (!targetTown.getClaimedChunks().isEmpty()) {
                    boolean hasOnline = false;
                    for (UUID memberId : targetTown.getMembers().keySet()) {
                        Player p = Bukkit.getPlayer(memberId);
                        if (p != null && p.isOnline()) {
                            hasOnline = true;
                            break;
                        }
                    }
                    if (hasOnline) {
                        RaidDifficulty[] difficulties = RaidDifficulty.values();
                        RaidDifficulty randomDifficulty = difficulties[random.nextInt(difficulties.length)];
                        startRaid(targetTown, randomDifficulty);
                    }
                }
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
    }

    public void startRaid(Town town, RaidDifficulty difficulty) {
        if (activeRaids.containsKey(town.getName().toLowerCase())) {
            return;
        }

        TownRaid raid = new TownRaid(town, 3, difficulty); // 3 waves for now
        activeRaids.put(town.getName().toLowerCase(), raid);

        BossBar bar = Bukkit.createBossBar("§c§lRAID [" + difficulty.getDisplayName() + "]: §f" + town.getName(),
                difficulty == RaidDifficulty.HARD ? BarColor.PURPLE
                        : (difficulty == RaidDifficulty.NORMAL ? BarColor.YELLOW : BarColor.RED),
                BarStyle.SOLID);
        raidBars.put(town.getName().toLowerCase(), bar);

        for (UUID memberUUID : town.getMembers().keySet()) {
            Player player = Bukkit.getPlayer(memberUUID);
            if (player != null) {
                bar.addPlayer(player);
                player.sendMessage("§c§l[RAID] §fYour town is under attack!");
                player.sendMessage("§7Defend the town against all waves to earn rewards.");
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!raid.isActive()) {
                    this.cancel();
                    return;
                }
                nextWave(raid);
            }
        }.runTaskLater(plugin, 100L); // Start first wave after 5 seconds
    }

    private void nextWave(TownRaid raid) {
        raid.setCurrentWave(raid.getCurrentWave() + 1);
        if (raid.getCurrentWave() > raid.getTotalWaves()) {
            finishRaid(raid, true);
            return;
        }

        Town town = raid.getTown();
        Set<Chunk> chunks = town.getClaimedChunksAsObjects();
        if (chunks.isEmpty()) {
            finishRaid(raid, false);
            return;
        }

        // Spawn logic: find a location at the edge
        Chunk[] chunkArray = chunks.toArray(new Chunk[0]);
        Chunk randomChunk = chunkArray[random.nextInt(chunkArray.length)];
        Location spawnLoc = randomChunk.getBlock(8, 0, 8).getLocation();
        spawnLoc.setY(spawnLoc.getWorld().getHighestBlockYAt(spawnLoc) + 1);

        int mobCount = 5 + (raid.getCurrentWave() * 2);
        if (raid.getDifficulty() == RaidDifficulty.NORMAL)
            mobCount = (int) (mobCount * 1.5);
        if (raid.getDifficulty() == RaidDifficulty.HARD)
            mobCount = (int) (mobCount * 2);

        for (int i = 0; i < mobCount; i++) {
            EntityType type = getRandomMobType(raid.getDifficulty());
            LivingEntity entity = (LivingEntity) spawnLoc.getWorld().spawnEntity(spawnLoc, type);

            String name = getMobName(type, raid.getDifficulty());
            entity.setCustomName(name);
            entity.setCustomNameVisible(true);

            if (raid.getDifficulty() == RaidDifficulty.HARD) {
                applyHardArmor(entity);
            }

            raid.addMob(entity);
        }

        updateBossBar(raid);
        broadcastToTown(town, "§c§l[RAID] §fWave " + raid.getCurrentWave() + " has started!");
    }

    public void onMobKill(UUID mobUUID) {
        for (TownRaid raid : activeRaids.values()) {
            if (raid.getActiveMobs().contains(mobUUID)) {
                raid.removeMob(mobUUID);
                updateBossBar(raid);

                if (raid.getRemainingMobs() == 0) {
                    broadcastToTown(raid.getTown(), "§a§l[RAID] §fWave " + raid.getCurrentWave() + " cleared!");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (raid.isActive())
                                nextWave(raid);
                        }
                    }.runTaskLater(plugin, 200L); // 10 seconds between waves
                }
                break;
            }
        }
    }

    private void updateBossBar(TownRaid raid) {
        BossBar bar = raidBars.get(raid.getTown().getName().toLowerCase());
        if (bar != null) {
            bar.setTitle("§c§lRAID: §fWave " + raid.getCurrentWave() + "/" + raid.getTotalWaves() + " §7("
                    + raid.getRemainingMobs() + " left)");
            double progress = (double) raid.getCurrentWave() / raid.getTotalWaves();
            bar.setProgress(Math.min(1.0, Math.max(0.0, progress)));
        }
    }

    public void finishRaid(TownRaid raid, boolean victory) {
        raid.setActive(false);
        activeRaids.remove(raid.getTown().getName().toLowerCase());

        BossBar bar = raidBars.remove(raid.getTown().getName().toLowerCase());
        if (bar != null)
            bar.removeAll();

        if (victory) {
            int xpReward = (int) (raid.getDifficulty().getBaseXP() * raid.getDifficulty().getMultiplier());
            broadcastToTown(raid.getTown(), "§a§l[RAID] §fVICTORY! Your town successfully defended itself.");
            broadcastToTown(raid.getTown(), "§e§l+ " + xpReward + " XP §7for all participating defenders!");

            // Grant XP to online members OR non-member contributors
            Set<UUID> allRecipients = new HashSet<>(raid.getTown().getMembers().keySet());
            allRecipients.addAll(raid.getContributors());

            for (UUID uuid : allRecipients) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    PlayerManager.getData(uuid).addXP(xpReward);
                    PlayerManager.savePlayer(uuid);
                    player.sendMessage(
                            "§a§l[!] §fYou earned §e" + xpReward + " XP§f for defending the town from a raid!");
                }
            }
        } else {
            broadcastToTown(raid.getTown(), "§c§l[RAID] §fFAILURE! The town has fallen.");
        }
    }

    public void addContributorIfRaidMob(UUID mobUUID, UUID playerUUID) {
        for (TownRaid raid : activeRaids.values()) {
            if (raid.getActiveMobs().contains(mobUUID)) {
                raid.addContributor(playerUUID);
                break;
            }
        }
    }

    public void forceStop(Town town) {
        TownRaid raid = activeRaids.get(town.getName().toLowerCase());
        if (raid != null) {
            // Remove surviving active mobs
            for (UUID mobId : raid.getActiveMobs()) {
                org.bukkit.entity.Entity mob = Bukkit.getEntity(mobId);
                if (mob != null) {
                    mob.remove();
                }
            }
            raid.getActiveMobs().clear();
            finishRaid(raid, false);
            broadcastToTown(town, "§e§l[RAID] §fThe raid was forcefully stopped by an admin.");
        }
    }

    public void forceStopAll() {
        // Create a copy list to avoid ConcurrentModificationException since forceStop
        // modifies the map
        List<Town> townsToStop = new ArrayList<>();
        for (TownRaid raid : activeRaids.values()) {
            townsToStop.add(raid.getTown());
        }
        for (Town town : townsToStop) {
            forceStop(town);
        }
    }

    private EntityType getRandomMobType(RaidDifficulty difficulty) {
        List<EntityType> types = new ArrayList<>(Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON));
        if (difficulty != RaidDifficulty.EASY) {
            types.add(EntityType.PILLAGER);
            types.add(EntityType.SPIDER);
        }
        if (difficulty == RaidDifficulty.HARD) {
            types.add(EntityType.VINDICATOR);
            types.add(EntityType.WITCH);
        }
        return types.get(random.nextInt(types.size()));
    }

    private String getMobName(EntityType type, RaidDifficulty difficulty) {
        String color = difficulty == RaidDifficulty.HARD ? "§5" : (difficulty == RaidDifficulty.NORMAL ? "§e" : "§f");
        String prefix = "";
        switch (type) {
            case ZOMBIE -> prefix = random.nextBoolean() ? "Undead" : "Rotting";
            case SKELETON -> prefix = random.nextBoolean() ? "Vengeful" : "Bone";
            case PILLAGER -> prefix = "Elite";
            case SPIDER -> prefix = "Web";
            case VINDICATOR -> prefix = "Merciless";
            case WITCH -> prefix = "Cursed";
            default -> prefix = "Raid";
        }
        return "§c[Lvl " + (difficulty.ordinal() + 1) + "] " + color + prefix + " " + type.name().substring(0, 1)
                + type.name().substring(1).toLowerCase();
    }

    private void applyHardArmor(LivingEntity entity) {
        EntityEquipment inv = entity.getEquipment();
        if (inv == null)
            return;

        inv.setHelmet(new ItemStack(Material.IRON_HELMET));
        inv.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        inv.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        inv.setBoots(new ItemStack(Material.IRON_BOOTS));

        // Randomly give sword/bow
        if (entity.getType() == EntityType.ZOMBIE) {
            inv.setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        }
    }

    private void broadcastToTown(Town town, String message) {
        for (UUID memberUUID : town.getMembers().keySet()) {
            Player player = Bukkit.getPlayer(memberUUID);
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    public boolean isRaidMob(UUID uuid) {
        for (TownRaid raid : activeRaids.values()) {
            if (raid.getActiveMobs().contains(uuid))
                return true;
        }
        return false;
    }
}
