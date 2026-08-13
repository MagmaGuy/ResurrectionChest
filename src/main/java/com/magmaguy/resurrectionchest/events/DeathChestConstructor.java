package com.magmaguy.resurrectionchest.events;

import com.magmaguy.resurrectionchest.ResurrectionChest;
import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import com.magmaguy.magmacore.location.LocationQueryRegistry;
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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

public class DeathChestConstructor implements Listener {
    private final Map<SignChangeEvent, PendingChestCreation> pendingCreations = new IdentityHashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void prepareSignPlace(SignChangeEvent event) {

        if (!event.getPlayer().hasPermission("resurrectionchest.use")) return;
        if (DefaultConfig.blacklistedWorlds.contains(event.getBlock().getLocation().getWorld().getName())) return;
        if (!event.getLine(0).equalsIgnoreCase(DefaultConfig.resurrectionChestSignName) &&
                !event.getLine(1).equalsIgnoreCase(DefaultConfig.resurrectionChestSignName))
            return;
        if (!(event.getBlock().getBlockData() instanceof WallSign sign))
            return;

        Block attached = event.getBlock().getRelative(sign.getFacing().getOppositeFace());
        if (!(attached.getBlockData() instanceof org.bukkit.block.data.type.Chest chest)) return;
        if (attached.getType() != Material.CHEST) return;
        if (isClaimedByAnotherPlayer(attached, event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(ChatColor.RED + "That chest is already registered as another player's Resurrection Chest.");
            return;
        }

        // Registering a resurrection chest claims a block as the player's item and XP
        // store, so it must obey the same region protection as building there.
        // LocationQueryRegistry wraps WorldGuard and GriefPrevention, fails closed on
        // adapter errors, and no-ops when neither plugin is installed.
        if (!LocationQueryRegistry.canBuild(event.getPlayer(), attached.getLocation())) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', DefaultConfig.chestProtectedRegionMessage));
            return;
        }

        Location spawnLocation = attached.getLocation().clone();
        // Set yaw from the chest's facing; the yaw orients the optional custom
        // chest model so it lines up with the chest block.
        spawnLocation.setYaw(blockFaceToYaw(chest.getFacing()));

        event.setLine(0, "");
        event.setLine(1, ChatColor.translateAlternateColorCodes('&', "&5" + DefaultConfig.resurrectionChestSignName));
        event.setLine(2, event.getPlayer().getDisplayName());
        event.setLine(3, "");
        pendingCreations.put(event, new PendingChestCreation(event.getPlayer(), spawnLocation));
        // MONITOR normally removes this during the same event dispatch. This
        // fallback prevents an event-dispatch exception from retaining the event.
        Bukkit.getScheduler().runTask(ResurrectionChest.plugin, () -> pendingCreations.remove(event));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void commitSignPlace(SignChangeEvent event) {
        PendingChestCreation pendingCreation = pendingCreations.remove(event);
        if (pendingCreation == null || event.isCancelled()) return;

        Block chestBlock = pendingCreation.spawnLocation().getBlock();
        if (chestBlock.getType() != Material.CHEST ||
                isClaimedByAnotherPlayer(chestBlock, pendingCreation.player().getUniqueId())) {
            pendingCreation.player().sendMessage(ChatColor.RED + "That chest is already registered as another player's Resurrection Chest.");
            return;
        }

        ResurrectionChestObject existingChest = ResurrectionChestObject.getResurrectionChest(pendingCreation.player());
        if (existingChest != null) existingChest.remove();
        ResurrectionChestObject resurrectionChestObject =
                new ResurrectionChestObject(pendingCreation.player(), pendingCreation.spawnLocation());
        Bukkit.getScheduler().runTask(ResurrectionChest.plugin, () -> {
            if (!resurrectionChestObject.isCurrentRegistration()) return;
            if (!resurrectionChestObject.markTrackedBlocks()) {
                ResurrectionChest.plugin.getLogger().warning(
                        "Could not claim the new ResurrectionChest because its block ownership tags conflict.");
                resurrectionChestObject.remove();
            }
        });

        pendingCreation.player().sendMessage(ChatColor.translateAlternateColorCodes('&', DefaultConfig.chestCreationMessage));
    }

    private record PendingChestCreation(org.bukkit.entity.Player player, Location spawnLocation) {
    }

    private boolean isClaimedByAnotherPlayer(Block chestBlock, UUID playerUuid) {
        if (!(chestBlock.getState() instanceof Chest chest)) return false;

        if (chest.getInventory() instanceof DoubleChestInventory doubleChestInventory) {
            return isClaimedByAnotherPlayer(doubleChestInventory.getLeftSide().getLocation(), playerUuid) ||
                    isClaimedByAnotherPlayer(doubleChestInventory.getRightSide().getLocation(), playerUuid);
        }

        return isClaimedByAnotherPlayer(chestBlock.getLocation(), playerUuid);
    }

    private boolean isClaimedByAnotherPlayer(Location location, UUID playerUuid) {
        if (ResurrectionChestObject.hasTrackedChestConflict(location.getBlock(), playerUuid))
            return true;
        ResurrectionChestObject owner = ResurrectionChestObject.getResurrectionChest(location);
        if (owner == null) return false;
        if (owner.isTrackingClaimPending()) {
            return !owner.getUuid().equals(playerUuid);
        }
        if (!owner.isTrackedChest(location.getBlock())) {
            owner.remove();
            return false;
        }
        return !owner.getUuid().equals(playerUuid);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlaceChest(BlockPlaceEvent event) {
        if (!event.getBlock().getType().equals(Material.CHEST)) return;
        Bukkit.getScheduler().runTaskLater(ResurrectionChest.plugin, () -> {
            BlockState chestState = event.getBlock().getState();
            if (chestState instanceof Chest && ((Chest) chestState).getInventory() instanceof DoubleChestInventory doubleChestInventory) {
                // Resolve both halves before notifying: both usually map to the same
                // registered chest, and notifying it twice would tear down and respawn
                // its custom model twice per placement.
                ResurrectionChestObject leftChest = ResurrectionChestObject.getResurrectionChest(doubleChestInventory.getLeftSide().getLocation());
                ResurrectionChestObject rightChest = ResurrectionChestObject.getResurrectionChest(doubleChestInventory.getRightSide().getLocation());
                if (leftChest != null) {
                    leftChest.markChestChanged();
                }
                if (rightChest != null && rightChest != leftChest) {
                    rightChest.markChestChanged();
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
