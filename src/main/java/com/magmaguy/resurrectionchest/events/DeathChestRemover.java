package com.magmaguy.resurrectionchest.events;

import com.magmaguy.resurrectionchest.ResurrectionChest;
import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

public class DeathChestRemover implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeathChestBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();
        boolean chest = brokenBlock.getType().equals(Material.CHEST);
        boolean sign = brokenBlock.getType().toString().endsWith("_SIGN");
        if (!chest && !sign) return;

        ResurrectionChestObject resurrectionChest = ResurrectionChestObject.getResurrectionChest(brokenBlock.getLocation());
        if (resurrectionChest == null) return;

        // MONITOR establishes the final cancellation state. The block itself is
        // changed after the event returns, so wait a tick and only tear down the
        // registration if the break actually took effect.
        var brokenLocation = brokenBlock.getLocation();
        Bukkit.getScheduler().runTask(ResurrectionChest.plugin, () -> {
            Block currentBlock = brokenLocation.getBlock();
            if (chest && resurrectionChest.isTrackedChest(currentBlock)) return;
            if (sign && currentBlock.getType().toString().endsWith("_SIGN") &&
                    resurrectionChest.isTrackedSign(currentBlock)) return;
            if (ResurrectionChestObject.getResurrectionChests().get(resurrectionChest.getUuid()) != resurrectionChest)
                return;
            resurrectionChest.remove();
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onEditSign(SignChangeEvent event) {
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
