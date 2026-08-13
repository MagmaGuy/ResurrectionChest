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

import java.util.Map;
import java.util.UUID;

public class PlayerDataConfig extends ConfigurationFile {

    public static final int TRACKED_BLOCK_SCHEMA_VERSION = 1;

    @Getter
    private static PlayerDataConfig instance;

    public record RawPlayerData(String locationString, String chestModel, int trackedBlockSchemaVersion) {
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
        String playerPath = uuid.toString();
        instance.fileConfiguration.set(playerPath + ".location", LocationParser.serializeLocation(location));
        instance.fileConfiguration.set(playerPath + ".chestModel", chestModel);
        instance.fileConfiguration.set(playerPath + ".trackedBlockSchemaVersion", TRACKED_BLOCK_SCHEMA_VERSION);
        ConfigurationEngine.fileSaverCustomValues(instance.fileConfiguration, instance.file);
    }

    public static void markTrackedBlockSchemaCurrent(UUID uuid) {
        instance.fileConfiguration.set(uuid + ".trackedBlockSchemaVersion", TRACKED_BLOCK_SCHEMA_VERSION);
        ConfigurationEngine.fileSaverCustomValues(instance.fileConfiguration, instance.file);
    }

    public static RawPlayerData getRawPlayerData(UUID uuid) {
        Map<String, Object> data = instance.fileConfiguration.getConfigurationSection(uuid.toString()).getValues(false);
        String locationString = (String) data.get("location");
        String chestModel = (String) data.get("chestModel");
        Object rawSchemaVersion = data.get("trackedBlockSchemaVersion");
        int trackedBlockSchemaVersion = rawSchemaVersion instanceof Number number
                ? number.intValue()
                : 0;
        return new RawPlayerData(locationString, chestModel, trackedBlockSchemaVersion);
    }

    public static void unregisterDeathChestEntry(ResurrectionChestObject resurrectionChestObject) {
        PlayerDataConfig.removePlayerData(resurrectionChestObject.getUuid());

        Player player = Bukkit.getPlayer(resurrectionChestObject.getUuid());
        if (player != null && player.isOnline())
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', DefaultConfig.chestDestructionMessage));
    }

    @Override
    public void initializeValues() {
    }
}
