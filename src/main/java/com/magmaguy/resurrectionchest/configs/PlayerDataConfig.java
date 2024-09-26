package com.magmaguy.resurrectionchest.configs;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.UUID;

public class PlayerDataConfig {

    public static FileConfiguration fileConfiguration;
    private static File file;

    public static void initializeConfig() {
        file = ConfigurationEngine.fileCreator("playerData.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

    public static void removePlayerData(UUID uuid) {
        fileConfiguration.set(uuid.toString(), null);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

    public static void addPlayerdata(UUID uuid, Location location){
        fileConfiguration.set(uuid.toString(), location);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

}
