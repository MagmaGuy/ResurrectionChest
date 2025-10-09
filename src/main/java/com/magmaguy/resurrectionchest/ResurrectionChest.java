package com.magmaguy.resurrectionchest;

import com.magmaguy.magmacore.MagmaCore;
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
        MetadataHandler.PLUGIN = plugin = Bukkit.getPluginManager().getPlugin("ResurrectionChest");

        Bukkit.getLogger().info("\n" +
                "                                                                       \n" +
                " _____                             _   _         _____ _           _   \n" +
                "| __  |___ ___ _ _ ___ ___ ___ ___| |_|_|___ ___|     | |_ ___ ___| |_ \n" +
                "|    -| -_|_ -| | |  _|  _| -_|  _|  _| | . |   |   --|   | -_|_ -|  _|\n" +
                "|__|__|___|___|___|_| |_| |___|___|_| |_|___|_|_|_____|_|_|___|___|_|  \n" +
                "                                                                       \n");

        MagmaCore.onEnable();
        MagmaCore.initializeImporter();

        MagmaCore.checkVersionUpdate("57541", "https://nightbreak.io/plugin/resurrectionchest/");

        new DefaultConfig();
        new PlayerDataConfig();

        this.getServer().getPluginManager().registerEvents(new DeathChestConstructor(), this);
        this.getServer().getPluginManager().registerEvents(new DeathChestRemover(), this);
        this.getServer().getPluginManager().registerEvents(new DeathEvent(), this);
        this.getServer().getPluginManager().registerEvents(new PersistentObjectHandler.PersistentObjectHandlerEvents(), this);
        this.getServer().getPluginManager().registerEvents(this, this);

        ResurrectionChestObject.initializeConfigDeathchests();
        ResurrectionChestObject.startClock();
    }

    @Override
    public void onLoad() {
        MagmaCore.createInstance(this);
    }

    @Override
    public void onDisable() {
        MagmaCore.shutdown();
        PersistentObjectHandler.shutdown();
        ResurrectionChestObject.shutdown();
        ResurrectionChestObject.getResurrectionChests().values().forEach(ResurrectionChestObject::chunkUnload);
        ResurrectionChestObject.getResurrectionChests().clear();
    }

}
