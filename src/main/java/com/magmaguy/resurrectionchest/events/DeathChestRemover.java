package com.magmaguy.resurrectionchest.events;

import com.magmaguy.resurrectionchest.ResurrectionChest;
import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import com.magmaguy.resurrectionchest.configs.PlayerDataConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class DeathChestRemover implements Listener {

    @EventHandler
    public void onDeathChestBreak(BlockBreakEvent event) {

        if (event.isCancelled()) return;
        if (event.getBlock().getType() == Material.ACACIA_WALL_SIGN ||
                event.getBlock().getType() == Material.BIRCH_WALL_SIGN ||
                event.getBlock().getType() == Material.DARK_OAK_WALL_SIGN ||
                event.getBlock().getType() == Material.JUNGLE_WALL_SIGN ||
                event.getBlock().getType() == Material.OAK_WALL_SIGN ||
                event.getBlock().getType() == Material.SPRUCE_WALL_SIGN) {

            WallSign sign = (WallSign) event.getBlock().getBlockData();
            Block attached = event.getBlock().getRelative(sign.getFacing().getOppositeFace());

            Location attachedLocation = attached.getLocation();

            ResurrectionChestObject resurrectionChest = ResurrectionChestObject.getResurrectionChest(attachedLocation);
            if (resurrectionChest != null)
                unregisterDeathChestEntry(attachedLocation);

        }


        if (event.getBlock().getType().equals(Material.CHEST))
            unregisterDeathChestEntry(event.getBlock().getLocation());

    }

    public static void unregisterDeathChestEntry(Location location) {

        ResurrectionChestObject resurrectionChestObject = ResurrectionChestObject.getResurrectionChest(location);
        if (resurrectionChestObject == null) return;

        PlayerDataConfig.removePlayerData(resurrectionChestObject.getUuid());

        if (Bukkit.getPlayer(resurrectionChestObject.getUuid()).isOnline())
            Bukkit.getPlayer(resurrectionChestObject.getUuid()).sendMessage(ChatColor.translateAlternateColorCodes('&', DefaultConfig.chestDestructionMessage));

        ResurrectionChestObject.getResurrectionChests().remove(resurrectionChestObject.getUuid());

    }

}
