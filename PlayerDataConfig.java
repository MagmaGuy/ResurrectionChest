package com.magmaguy.resurrectionchest;

import org.bukkit.configuration.Configuration;

public class PlayerDataConfig {

    public static final String CONFIG_NAME = "playerData.yml";
    public CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    public Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        //no real defaults, just a data file
        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
