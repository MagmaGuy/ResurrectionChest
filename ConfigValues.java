package com.magmaguy.resurrectionchest;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;

public class ConfigValues {

    public static Configuration defaultConfig, playerDataConfig;

    public static void initializeConfigValues() {

        CustomConfigLoader customConfigLoader = new CustomConfigLoader();

        defaultConfig = Bukkit.getPluginManager().getPlugin("ResurrectionChest").getConfig();

        playerDataConfig = customConfigLoader.getCustomConfig(PlayerDataConfig.CONFIG_NAME);

    }

}
