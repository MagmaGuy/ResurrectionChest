package com.magmaguy.resurrectionchest;

import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import com.magmaguy.resurrectionchest.configs.PlayerDataConfig;
import com.magmaguy.resurrectionchest.events.DeathChestConstructor;
import com.magmaguy.resurrectionchest.events.DeathChestRemover;
import com.magmaguy.resurrectionchest.events.DeathEvent;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ResurrectionChest extends JavaPlugin implements Listener {

    public static Plugin plugin = null;

    @Override
    public void onEnable() {
        new Metrics(this, 2677);
        plugin = Bukkit.getPluginManager().getPlugin("ResurrectionChest");

        DefaultConfig.loadConfiguration();
        PlayerDataConfig.initializeConfig();

        this.getServer().getPluginManager().registerEvents(new DeathChestConstructor(), this);
        this.getServer().getPluginManager().registerEvents(new DeathChestRemover(), this);
        this.getServer().getPluginManager().registerEvents(new DeathEvent(), this);
        this.getServer().getPluginManager().registerEvents(new ChunkEntity.ChunkEntityEvents(), this);
        this.getServer().getPluginManager().registerEvents(this, this);

        ResurrectionChestObject.initializeConfigDeathchests();
    }

    @Override
    public void onDisable() {

    }

}
