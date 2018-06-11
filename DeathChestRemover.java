package com.magmaguy.resurrectionchest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.material.Sign;

import java.util.UUID;

public class DeathChestRemover implements Listener {

    @EventHandler
    public void onDeathChestBreak(BlockBreakEvent event) {

        if (event.isCancelled()) return;
        if (event.getBlock().getType().equals(Material.WALL_SIGN)) {

            if (((org.bukkit.block.Sign) event.getBlock().getState()).getLine(1)
                    .equals(ChatColor.translateAlternateColorCodes('&', "&5" +
                            ConfigValues.defaultConfig.getString(DefaultConfig.RESURRECTION_CHEST_SIGN_NAME)))) {

                Sign sign = (Sign) event.getBlock().getState().getData();
                Block attached = event.getBlock().getRelative(sign.getAttachedFace());

                Location attachedLocation = attached.getLocation();

                unregisterDeathChestEntry(attachedLocation);

                return;

            }

        }

        if (event.getBlock().getType().equals(Material.CHEST)) {

            unregisterDeathChestEntry(event.getBlock().getLocation());

        }

    }

    public static void unregisterDeathChestEntry(Location location) {

        if (ConfigValues.playerDataConfig.getValues(true).containsValue(location.toString())) {

            for (String string : ConfigValues.playerDataConfig.getValues(true).keySet()) {

                if (ConfigValues.playerDataConfig.get(string).equals(location.toString())) {

                    PlayerDataConfig playerDataConfig = new PlayerDataConfig();
                    ConfigValues.playerDataConfig.set(string, null);
                    playerDataConfig.configuration.set(string, null);
                    playerDataConfig.customConfigLoader.saveCustomConfig(PlayerDataConfig.CONFIG_NAME);
                    playerDataConfig.customConfigLoader.saveDefaultCustomConfig(PlayerDataConfig.CONFIG_NAME);

                    ConfigValues.initializeConfigValues();

                    if (Bukkit.getPlayer(UUID.fromString(string)).isOnline())
                        Bukkit.getPlayer(UUID.fromString(string)).sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigValues.defaultConfig.getString(DefaultConfig.CHEST_DESTRUCTION_MESSAGE)));

                }

            }

        }

    }

}
