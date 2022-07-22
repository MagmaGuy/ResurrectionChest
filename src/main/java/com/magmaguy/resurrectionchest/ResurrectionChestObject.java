package com.magmaguy.resurrectionchest;

import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import com.magmaguy.resurrectionchest.configs.PlayerDataConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class ResurrectionChestObject {
    private static final HashMap<UUID, ResurrectionChestObject> resurrectionChests = new HashMap<>();

    public static HashMap<UUID, ResurrectionChestObject> getResurrectionChests() {
        return resurrectionChests;
    }

    public static ResurrectionChestObject getResurrectionChest(Player player) {
        if (!resurrectionChests.containsKey(player.getUniqueId()))
            return null;
        return resurrectionChests.get(player.getUniqueId());
    }

    public static ResurrectionChestObject getResurrectionChest(Location location) {
        for (ResurrectionChestObject resurrectionChestObject : resurrectionChests.values())
            if (location.equals(resurrectionChestObject.getLocation()))
                return resurrectionChestObject;
        return null;
    }

    private final UUID uuid;
    private final Location location;

    private BukkitTask visualEffectTask = null;

    public void initializeParticleEffects() {
        if (!DefaultConfig.enableParticleEffects) return;
        if (visualEffectTask != null) return;

        Location adjustedLocation = location.clone().add(0.5, 0.5, 0.5);

        visualEffectTask = new BukkitRunnable() {
            @Override
            public void run() {
                location.getWorld().spawnParticle(Particle.valueOf(DefaultConfig.particleEffect1), adjustedLocation, 1, 0.075, 0.075, 0.75, 0.8);
                location.getWorld().spawnParticle(Particle.valueOf(DefaultConfig.particleEffect2), adjustedLocation, 1, 0.075, 0.075, 0.75, 0.8);
                location.getWorld().spawnParticle(Particle.valueOf(DefaultConfig.particleEffect3), adjustedLocation, 1, 0.075, 0.075, 0.75, 0.8);
            }

        }.runTaskTimer(ResurrectionChest.plugin, 1, 1);

    }

    public ResurrectionChestObject(UUID uuid, Location location) {
        this.uuid = uuid;
        this.location = location;
        resurrectionChests.put(uuid, this);
        new ChunkEntity(location, this);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Location getLocation() {
        return location;
    }

    public void load() {
        initializeParticleEffects();
        visualEffectTask = null;
    }

    public void unload() {
        if (visualEffectTask == null) return;
        visualEffectTask.cancel();
        visualEffectTask = null;
    }

    public static void initializeConfigDeathchests() {
        for (String uuidString : PlayerDataConfig.fileConfiguration.getKeys(false))
            new ResurrectionChestObject(UUID.fromString(uuidString), LocationParser.parseLocation(PlayerDataConfig.fileConfiguration.getString(uuidString)));
    }
}
