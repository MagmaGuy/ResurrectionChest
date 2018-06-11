package com.magmaguy.resurrectionchest;

import org.bstats.Metrics;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ResurrectionChest extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {

        Metrics metrics = new Metrics(this);

        DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.loadConfiguration();

        PlayerDataConfig playerDataConfig = new PlayerDataConfig();
        playerDataConfig.initializeConfig();

        ConfigValues.initializeConfigValues();

        this.getServer().getPluginManager().registerEvents(new DeathChestConstructor(), this);
        this.getServer().getPluginManager().registerEvents(new DeathChestRemover(), this);
        this.getServer().getPluginManager().registerEvents(new DeathEvent(), this);
        this.getServer().getPluginManager().registerEvents(this, this);

        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.ENABLE_PARTICLE_EFFECTS)) VisualEffect.initializeParticleEffects();

    }

    @Override
    public void onDisable() {
    }

}
