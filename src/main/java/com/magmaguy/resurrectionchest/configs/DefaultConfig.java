package com.magmaguy.resurrectionchest.configs;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class DefaultConfig {

    public static String resurrectionChestSignName;
    public static boolean enableHighCompatibility;
    public static boolean enableDurabilityLossOnDeath;
    public static int durabilityToLower;
    public static String chestCreationMessage;
    public static String chestDestructionMessage;
    public static String chestMissingMessage;
    public static String deathMessage;
    public static boolean enableParticleEffects;
    public static String particleEffect1;
    public static String particleEffect2;
    public static String particleEffect3;
    public static List<String> blacklistedWorlds;
    public static boolean storeXP;
    public static double xpPercentage;

    public static void loadConfiguration() {
        File file = ConfigurationEngine.fileCreator("config.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        resurrectionChestSignName = ConfigurationEngine.setString(fileConfiguration, "Input name for death chest", "[DeathChest]");
        enableHighCompatibility = ConfigurationEngine.setBoolean(fileConfiguration, "Enable high compatibility / low security mode for plugin conflicts", false);
        enableDurabilityLossOnDeath = ConfigurationEngine.setBoolean(fileConfiguration, "Lower worn armor's durability on death", true);
        durabilityToLower = ConfigurationEngine.setInt(fileConfiguration, "Amount of durability to lower on death", 100);
        chestCreationMessage = ConfigurationEngine.setString(fileConfiguration, "Chest creation message", "&aYou've created your Death Chest!");
        chestDestructionMessage = ConfigurationEngine.setString(fileConfiguration, "Chest destruction message", "&cYour Death Chest has been destroyed!");
        chestMissingMessage = ConfigurationEngine.setString(fileConfiguration, "Chest missing message", "&4Your Death Chest is missing!");
        deathMessage = ConfigurationEngine.setString(fileConfiguration, "Death message", "&aYour items have been moved to your Death Chest!");
        enableParticleEffects = ConfigurationEngine.setBoolean(fileConfiguration, "Enable particle effects for death chests", true);
        particleEffect1 = ConfigurationEngine.setString(fileConfiguration, "Particle effect 1", "ENCHANTMENT_TABLE");
        particleEffect2 = ConfigurationEngine.setString(fileConfiguration, "Particle effect 2", "ENCHANTMENT_TABLE");
        particleEffect3 = ConfigurationEngine.setString(fileConfiguration, "Particle effect 3", "PORTAL");
        blacklistedWorlds = ConfigurationEngine.setStringList(fileConfiguration, "blacklistedWorlds", Arrays.asList("none"));
        storeXP = ConfigurationEngine.setBoolean(fileConfiguration, "storeXP", true);
        xpPercentage = ConfigurationEngine.setDouble(fileConfiguration, "xpPercentageKept", 0.75);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

    public static void reloadConfig() {

        Bukkit.getPluginManager().getPlugin("ResurrectionChest").reloadConfig();

    }

}
