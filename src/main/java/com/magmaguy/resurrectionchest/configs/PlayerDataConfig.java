package com.magmaguy.resurrectionchest.configs;

import com.magmaguy.magmacore.config.ConfigurationEngine;
import com.magmaguy.magmacore.config.ConfigurationFile;
import com.magmaguy.resurrectionchest.LocationParser;
import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataConfig extends ConfigurationFile {

    @Getter
    private static PlayerDataConfig instance;

    public record PlayerData(Location location, String chestModel) {
    }

    public record RawPlayerData(String locationString, String chestModel) {
    }

    public PlayerDataConfig() {
        super("playerData.yml");
        instance = this;
    }

    public static void removePlayerData(UUID uuid) {
        instance.fileConfiguration.set(uuid.toString(), null);
        ConfigurationEngine.fileSaverCustomValues(instance.fileConfiguration, instance.file);
    }

    public static void addPlayerdata(UUID uuid, Location location, String chestModel) {
        Map<String, Object> playerData = new HashMap<>();
        playerData.put("location", location.toString());
        playerData.put("chestModel", chestModel);
        instance.fileConfiguration.set(uuid.toString(), playerData);
        ConfigurationEngine.fileSaverCustomValues(instance.fileConfiguration, instance.file);
    }

    public static PlayerData getPlayerData(UUID uuid) {
        Map<String, Object> data = instance.fileConfiguration.getConfigurationSection(uuid.toString()).getValues(false);
        Location location = LocationParser.parseLocation((String) data.get("location"));
        String chestModel = (String) data.get("chestModel");
        return new PlayerData(location, chestModel);
    }

    public static RawPlayerData getRawPlayerData(UUID uuid) {
        Map<String, Object> data = instance.fileConfiguration.getConfigurationSection(uuid.toString()).getValues(false);
        String locationString = (String) data.get("location");
        String chestModel = (String) data.get("chestModel");
        return new RawPlayerData(locationString, chestModel);
    }

    public static void unregisterDeathChestEntry(ResurrectionChestObject resurrectionChestObject) {
        PlayerDataConfig.removePlayerData(resurrectionChestObject.getUuid());

        Player player = Bukkit.getPlayer(resurrectionChestObject.getUuid());
        if (player != null && player.isOnline())
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', DefaultConfig.chestDestructionMessage));

        ResurrectionChestObject.getResurrectionChests().remove(resurrectionChestObject.getUuid());
    }

    @Override
    public void initializeValues() {
    }
}