package com.magmaguy.resurrectionchest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.material.Sign;

public class DeathChestConstructor implements Listener {

    @EventHandler
    public void onSignPlace(SignChangeEvent event) {

        if (!event.getPlayer().hasPermission("resurrectionchest.use")) return;
        if (!event.getLine(0).equalsIgnoreCase(ConfigValues.defaultConfig.getString(DefaultConfig.RESURRECTION_CHEST_SIGN_NAME)) &&
                !event.getLine(1).equalsIgnoreCase(ConfigValues.defaultConfig.getString(DefaultConfig.RESURRECTION_CHEST_SIGN_NAME))) return;
        if (event.getBlock().getType() != Material.WALL_SIGN) return;
        Sign sign = (Sign) event.getBlock().getState().getData();
        Block attached = event.getBlock().getRelative(sign.getAttachedFace());
        if (attached.getType() != Material.CHEST) return;

        String identifier = event.getPlayer().getUniqueId().toString();

        //set config value
        PlayerDataConfig playerDataConfig = new PlayerDataConfig();
        ConfigValues.playerDataConfig.set(identifier, attached.getLocation().toString());
        playerDataConfig.configuration.set(identifier, attached.getLocation().toString());
        playerDataConfig.customConfigLoader.saveCustomConfig(PlayerDataConfig.CONFIG_NAME);
        playerDataConfig.customConfigLoader.saveDefaultCustomConfig(PlayerDataConfig.CONFIG_NAME);

        ConfigValues.initializeConfigValues();

        event.setLine(0, "");
        event.setLine(1, ChatColor.translateAlternateColorCodes('&', "&5" + ConfigValues.defaultConfig.getString(DefaultConfig.RESURRECTION_CHEST_SIGN_NAME)));
        event.setLine(2, event.getPlayer().getDisplayName());
        event.setLine(3, "");

        Bukkit.getLogger().info("[ResurrectionChest] Creating new ResurrectionChest for " + event.getPlayer().getName());
        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigValues.defaultConfig.getString(DefaultConfig.CHEST_CREATION_MESSAGE)));

    }

}
