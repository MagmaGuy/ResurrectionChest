package com.magmaguy.resurrectionchest.tasks;

import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import com.magmaguy.resurrectionchest.utils.ChunkLocationDetector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class VisualEffect implements Listener {

    public static HashSet<Location> loadedLocations = new HashSet<>();

    @EventHandler
    public void chunkLoadEvent(ChunkLoadEvent chunkLoadEvent) {
        for (ResurrectionChestObject resurrectionChestObject : ResurrectionChestObject.getResurrectionChests().values())
            if (resurrectionChestObject != null &&
                    resurrectionChestObject.getLocation() != null &&
                    ChunkLocationDetector.chunkLocationCheck(resurrectionChestObject.getLocation(), chunkLoadEvent.getChunk())) {
                loadedLocations.add(resurrectionChestObject.getLocation());
                initializeParticleEffects(resurrectionChestObject.getLocation());
            }
    }

    @EventHandler
    public void chunkUnloadEvent(ChunkUnloadEvent event) {
        Location toRemove = null;
        for (Location location : loadedLocations)
            if (ChunkLocationDetector.chunkLocationCheck(location, event.getChunk())) {
                toRemove = location;
                break;
            }
        if (toRemove != null)
            loadedLocations.remove(toRemove);
    }


    public static void initializeParticleEffects(Location location) {

        Location adjustedLocation = location.clone().add(0.5, 0.5, 0.5);

        new BukkitRunnable() {
            @Override
            public void run() {

                if (!loadedLocations.contains(location)) {
                    cancel();
                    return;
                }

                location.getWorld().spawnParticle(Particle.valueOf(DefaultConfig.particleEffect1), adjustedLocation, 1, 0.075, 0.075, 0.75, 0.8);
                location.getWorld().spawnParticle(Particle.valueOf(DefaultConfig.particleEffect2), adjustedLocation, 1, 0.075, 0.075, 0.75, 0.8);
                location.getWorld().spawnParticle(Particle.valueOf(DefaultConfig.particleEffect3), adjustedLocation, 1, 0.075, 0.075, 0.75, 0.8);

            }

        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("ResurrectionChest"), 1, 1);

    }

}
