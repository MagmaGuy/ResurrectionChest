package com.magmaguy.resurrectionchest.configs;

import com.magmaguy.magmacore.config.ConfigurationEngine;
import com.magmaguy.magmacore.config.ConfigurationFile;

import java.util.Arrays;
import java.util.List;

public class DefaultConfig extends ConfigurationFile {
    private static DefaultConfig instance;

    public DefaultConfig() {
        super("config.yml");
        instance = this;
    }

    public static boolean setupDone;
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
    public static String deathChestNameTag;
    public static String freeSingleDeathChestModelName;
    public static String freeDoubleDeathChestModelName;
    public static String premiumSingleDeathChestModelName;
    public static String premiumDoubleDeathChestModelName;
    public static String deathChestRemovedMessage;

    public static boolean isSetupDone() {
        return setupDone;
    }

    public static void toggleSetupDone(boolean value) {
        setupDone = value;
        ConfigurationEngine.writeValue(setupDone, instance.file, instance.getFileConfiguration(), "setupDone");
    }

    @Override
    public void initializeValues() {
        setupDone = ConfigurationEngine.setBoolean(fileConfiguration, "setupDone", false);
        resurrectionChestSignName = ConfigurationEngine.setString(fileConfiguration, "Input name for death chest", "[DeathChest]");
        enableHighCompatibility = ConfigurationEngine.setBoolean(fileConfiguration, "Enable high compatibility / low security mode for plugin conflicts", false);
        enableDurabilityLossOnDeath = ConfigurationEngine.setBoolean(fileConfiguration, "Lower worn armor's durability on death", true);
        durabilityToLower = ConfigurationEngine.setInt(fileConfiguration, "Amount of durability to lower on death", 100);
        chestCreationMessage = ConfigurationEngine.setString(fileConfiguration, "Chest creation message", "&8[ResurrectionChest] &aYou've created your Death Chest!");
        chestDestructionMessage = ConfigurationEngine.setString(fileConfiguration, "Chest destruction message", "&8[ResurrectionChest] &cYour Death Chest has been destroyed!");
        chestMissingMessage = ConfigurationEngine.setString(fileConfiguration, "Chest missing message", "&8[ResurrectionChest] &4Your Death Chest is missing!");
        deathMessage = ConfigurationEngine.setString(fileConfiguration, "Death message", "&8[ResurrectionChest] &aYour items have been moved to your Death Chest!");
        enableParticleEffects = ConfigurationEngine.setBoolean(fileConfiguration, "Enable particle effects for death chests", true);
        particleEffect1 = ConfigurationEngine.setString(fileConfiguration, "Particle effect 1", "ENCHANTMENT_TABLE");
        particleEffect2 = ConfigurationEngine.setString(fileConfiguration, "Particle effect 2", "ENCHANTMENT_TABLE");
        particleEffect3 = ConfigurationEngine.setString(fileConfiguration, "Particle effect 3", "PORTAL");
        blacklistedWorlds = ConfigurationEngine.setList(fileConfiguration, "blacklistedWorlds", Arrays.asList("none"));
        storeXP = ConfigurationEngine.setBoolean(fileConfiguration, "storeXP", true);
        xpPercentage = ConfigurationEngine.setDouble(fileConfiguration, "xpPercentageKept", 0.75);
        deathChestNameTag = ConfigurationEngine.setString(fileConfiguration, "deathChestNameTag", "$playerName's &fResurrection Chest");
        freeSingleDeathChestModelName = ConfigurationEngine.setString(fileConfiguration, "freeSingleDeathChestModelName", "resurrectionchest_free_single");
        freeDoubleDeathChestModelName = ConfigurationEngine.setString(fileConfiguration, "freeDoubleDeathChestModelName", "resurrectionchest_free_double");
        premiumSingleDeathChestModelName = ConfigurationEngine.setString(fileConfiguration, "premiumSingleDeathChestModelName", "resurrectionchest_angelic_single");
        premiumDoubleDeathChestModelName = ConfigurationEngine.setString(fileConfiguration, "premiumDoubleDeathChestModelName", "resurrectionchest_angelic_double");
        deathChestRemovedMessage = ConfigurationEngine.setString(fileConfiguration, "deathChestRemovedMessage", "&8[ResurrectionChest] &cYour Death Chest has been removed!");
    }
}
