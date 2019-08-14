package com.magmaguy.resurrectionchest;

import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import com.magmaguy.resurrectionchest.configs.PlayerDataConfig;
import com.magmaguy.resurrectionchest.events.DeathChestConstructor;
import com.magmaguy.resurrectionchest.events.DeathChestRemover;
import com.magmaguy.resurrectionchest.events.DeathEvent;
import com.magmaguy.resurrectionchest.tasks.VisualEffect;
import org.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ResurrectionChest extends JavaPlugin implements Listener {

    public static Plugin plugin = null;

    @Override
    public void onEnable() {

        Metrics metrics = new Metrics(this);

        plugin = Bukkit.getPluginManager().getPlugin("ResurrectionChest");

        DefaultConfig.loadConfiguration();
        PlayerDataConfig.initializeConfig();
        ResurrectionChestObject.initializeConfigDeathchests();

        this.getServer().getPluginManager().registerEvents(new DeathChestConstructor(), this);
        this.getServer().getPluginManager().registerEvents(new DeathChestRemover(), this);
        this.getServer().getPluginManager().registerEvents(new DeathEvent(), this);
        this.getServer().getPluginManager().registerEvents(this, this);

        if (DefaultConfig.enableParticleEffects)
            this.getServer().getPluginManager().registerEvents(new VisualEffect(), this);

        for (ResurrectionChestObject resurrectionChestObject : ResurrectionChestObject.getResurrectionChests().values())
            if (resurrectionChestObject.getLocation().isWorldLoaded())
                if (resurrectionChestObject.getLocation().getChunk().isLoaded()) {
                    VisualEffect.loadedLocations.add(resurrectionChestObject.getLocation());
                    VisualEffect.initializeParticleEffects(resurrectionChestObject.getLocation());
                }

    }

    @Override
    public void onDisable() {
    }

}
