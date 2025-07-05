package com.magmaguy.resurrectionchest.events;

import com.magmaguy.resurrectionchest.ResurrectionChest;
import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.DoubleChestInventory;

public class DeathChestConstructor implements Listener {

    @EventHandler
    public void onSignPlace(SignChangeEvent event) {

        if (!event.getPlayer().hasPermission("resurrectionchest.use")) return;
        if (DefaultConfig.blacklistedWorlds.contains(event.getBlock().getLocation().getWorld().getName())) return;
        if (!event.getLine(0).equalsIgnoreCase(DefaultConfig.resurrectionChestSignName) &&
                !event.getLine(1).equalsIgnoreCase(DefaultConfig.resurrectionChestSignName))
            return;
        if (!(event.getBlock().getBlockData() instanceof WallSign sign))
            return;

        Block attached = event.getBlock().getRelative(sign.getFacing().getOppositeFace());
        if (!(attached.getBlockData() instanceof org.bukkit.block.data.type.Chest chest)) return;

        Location spawnLocation = attached.getLocation().clone();
        // Set yaw based on the opposite face of where the sign is facing
        // This makes the player face away from the wall when they spawn
        spawnLocation.setYaw(blockFaceToYaw(chest.getFacing()));

        if (ResurrectionChestObject.getResurrectionChest(event.getPlayer()) != null)
            ResurrectionChestObject.getResurrectionChest(event.getPlayer()).remove();
        new ResurrectionChestObject(event.getPlayer(), spawnLocation);

        event.setLine(0, "");
        event.setLine(1, ChatColor.translateAlternateColorCodes('&', "&5" + DefaultConfig.resurrectionChestSignName));
        event.setLine(2, event.getPlayer().getDisplayName());
        event.setLine(3, "");

        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', DefaultConfig.chestCreationMessage));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlaceChest(BlockPlaceEvent event) {
        if (!event.getBlock().getType().equals(Material.CHEST)) return;
        Bukkit.getScheduler().runTaskLater(ResurrectionChest.plugin, () -> {
            BlockState chestState = event.getBlock().getState();
            if (chestState instanceof Chest && ((Chest) chestState).getInventory() instanceof DoubleChestInventory doubleChestInventory) {
                ResurrectionChestObject resurrectionChest = ResurrectionChestObject.getResurrectionChest(doubleChestInventory.getLeftSide().getLocation());
                if (resurrectionChest != null) {
                    resurrectionChest.setChestHasChanged(true);
                }
                resurrectionChest = ResurrectionChestObject.getResurrectionChest(doubleChestInventory.getRightSide().getLocation());
                if (resurrectionChest != null) {
                    resurrectionChest.setChestHasChanged(true);
                }
            }
        }, 1);
    }

    /**
     * Converts a BlockFace to the corresponding yaw value
     *
     * @param face The BlockFace to convert
     * @return The yaw value in degrees
     */
    private float blockFaceToYaw(BlockFace face) {
        switch (face) {
            case NORTH:
                return 180.0f;
            case SOUTH:
                return 0.0f;
            case EAST:
                return -90.0f;
            case WEST:
                return 90.0f;
            default:
                return 0.0f; // Default to south if unsupported face
        }
    }
}