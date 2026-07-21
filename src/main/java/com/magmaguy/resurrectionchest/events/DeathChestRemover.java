package com.magmaguy.resurrectionchest.events;

import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import com.magmaguy.resurrectionchest.configs.PlayerDataConfig;
import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

public class DeathChestRemover implements Listener {

    @EventHandler
    public void onDeathChestBreak(BlockBreakEvent event) {

        if (event.isCancelled()) return;
        if (!event.getBlock().getType().equals(Material.CHEST) && !event.getBlock().getType().toString().endsWith("_SIGN")) return;

        ResurrectionChestObject resurrectionChest = ResurrectionChestObject.getResurrectionChest(event.getBlock().getLocation());
        if (resurrectionChest == null) return;
        resurrectionChest.remove();
    }

    @EventHandler
    public void onEditSign(SignChangeEvent event) {
        if (event.isCancelled()) return;
        if (!event.getBlock().getType().toString().endsWith("WALL_SIGN")) return;

        ResurrectionChestObject resurrectionChest = ResurrectionChestObject.getResurrectionChest(event.getBlock().getLocation());
        if (resurrectionChest == null) return;
        // The creation event reaches this handler too, right after DeathChestConstructor
        // registers the chest. The sign only carries the tracking tag from the tick after
        // creation, so an untagged sign means this is the creation event - cancelling it
        // would discard the text DeathChestConstructor just set.
        if (!resurrectionChest.isTrackedSign(event.getBlock())) return;
        event.setCancelled(true);
    }
}
