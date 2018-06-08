package com.magmaguy.resurrectionchest;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;

public class DefaultConfig {

    public static void reloadConfig() {

        Bukkit.getPluginManager().getPlugin("ResurrectionChest").reloadConfig();

    }

    public void loadConfiguration() {

        Configuration configuration = Bukkit.getPluginManager().getPlugin("ResurrectionChest").getConfig();

        configuration.options().copyDefaults(true);

        Bukkit.getPluginManager().getPlugin("ResurrectionChest").saveConfig();
        Bukkit.getPluginManager().getPlugin("ResurrectionChest").saveDefaultConfig();

    }

}
