package com.magmaguy.resurrectionchest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class VisualEffect {

    public static void initializeParticleEffects() {

        new BukkitRunnable() {
            @Override
            public void run() {

                for (String string : ConfigValues.playerDataConfig.getKeys(true)) {

                    Location location = LocationParser.parseLocation(ConfigValues.playerDataConfig.getString(string));

                    try {

                        if (location.getChunk().isLoaded()) {

                            location.add(new Vector(0.5, 0.5, 0.5));

                            location.getWorld().spawnParticle(Particle.valueOf(ConfigValues.defaultConfig.getString(DefaultConfig.PARTICLE_EFFECT_1)), location, 1, 0.075, 0.075, 0.75, 0.8);
                            location.getWorld().spawnParticle(Particle.valueOf(ConfigValues.defaultConfig.getString(DefaultConfig.PARTICLE_EFFECT_1)), location, 1, 0.075, 0.075, 0.75, 0.8);
                            location.getWorld().spawnParticle(Particle.valueOf(ConfigValues.defaultConfig.getString(DefaultConfig.PARTICLE_EFFECT_2)), location, 1, 0.075, 0.075, 0.75, 0.8);

                        }


                    } catch (Exception ignored) {
                    }

                }

            }

        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("ResurrectionChest"), 1, 1);

    }

}
