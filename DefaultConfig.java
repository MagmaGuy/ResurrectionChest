package com.magmaguy.resurrectionchest;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;

public class DefaultConfig {

    public static final String RESURRECTION_CHEST_SIGN_NAME = "Input name for death chest";
    public static final String ENABLE_HIGH_COMPATIBILITY = "Enable high compatibility / low security mode for plugin conflicts";
    public static final String ENABLE_DURABILITY_LOSS_ON_DEATH = "Lower worn armor's durability on death";
    public static final String DURABILITY_TO_LOWER = "Amount of durability to lower on death";
    public static final String CHEST_CREATION_MESSAGE = "Chest creation message";
    public static final String CHEST_DESTRUCTION_MESSAGE = "Chest destruction message";
    public static final String CHEST_MISSING_MESSAGE = "Chest missing message";
    public static final String DEATH_MESSAGE = "Death message";
    public static final String ENABLE_PARTICLE_EFFECTS = "Enable particle effects for death chest";
    public static final String PARTICLE_EFFECT_1 = "Particle effect 1";
    public static final String PARTICLE_EFFECT_2 = "Particle effect 2";
    public static final String PARTICLE_EFFECT_3 = "Particle effect 3";

    public void loadConfiguration() {

        Configuration configuration = Bukkit.getPluginManager().getPlugin("ResurrectionChest").getConfig();

        configuration.addDefault(RESURRECTION_CHEST_SIGN_NAME, "[DeathChest]");
        configuration.addDefault(ENABLE_HIGH_COMPATIBILITY, false);
        configuration.addDefault(ENABLE_DURABILITY_LOSS_ON_DEATH, true);
        configuration.addDefault(DURABILITY_TO_LOWER, 100);
        configuration.addDefault(CHEST_CREATION_MESSAGE, "&aYou've created your Death Chest!");
        configuration.addDefault(CHEST_DESTRUCTION_MESSAGE, "&cYour Death Chest has been destroyed!");
        configuration.addDefault(CHEST_MISSING_MESSAGE, "&4Your Death Chest is missing!");
        configuration.addDefault(DEATH_MESSAGE, "&aYour items have been moved to your Death Chest!");
        configuration.addDefault(ENABLE_PARTICLE_EFFECTS, true);
        configuration.addDefault(PARTICLE_EFFECT_1, "ENCHANTMENT_TABLE");
        configuration.addDefault(PARTICLE_EFFECT_2, "ENCHANTMENT_TABLE");
        configuration.addDefault(PARTICLE_EFFECT_3, "PORTAL");

        configuration.options().copyDefaults(true);

        Bukkit.getPluginManager().getPlugin("ResurrectionChest").saveConfig();
        Bukkit.getPluginManager().getPlugin("ResurrectionChest").saveDefaultConfig();

    }

    public static void reloadConfig() {

        Bukkit.getPluginManager().getPlugin("ResurrectionChest").reloadConfig();

    }

}
