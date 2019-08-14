package com.magmaguy.resurrectionchest;

import com.magmaguy.resurrectionchest.configs.PlayerDataConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class ResurrectionChestObject {

    private static HashMap<UUID, ResurrectionChestObject> resurrectionChests = new HashMap<>();

    public static HashMap<UUID, ResurrectionChestObject> getResurrectionChests() {
        return resurrectionChests;
    }

    public static ResurrectionChestObject getResurrectionChest(Player player) {
        if (!resurrectionChests.containsKey(player.getUniqueId()))
            return null;
        return resurrectionChests.get(player.getUniqueId());
    }

    public static ResurrectionChestObject getResurrectionChest(Location location) {
        for (ResurrectionChestObject resurrectionChestObject : resurrectionChests.values())
            if (location.equals(resurrectionChestObject.getLocation()))
                return resurrectionChestObject;
        return null;
    }

    private UUID uuid;
    private Location location;

    public ResurrectionChestObject(UUID uuid, Location location) {
        this.uuid = uuid;
        this.location = location;
        resurrectionChests.put(uuid, this);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Location getLocation() {
        return location;
    }

    public static void initializeConfigDeathchests() {

        for (String uuidString : PlayerDataConfig.fileConfiguration.getKeys(false))
            new ResurrectionChestObject(UUID.fromString(uuidString), LocationParser.parseLocation(PlayerDataConfig.fileConfiguration.getString(uuidString)));

    }

}
