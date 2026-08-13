package com.magmaguy.resurrectionchest.events;

import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeathEvent implements Listener {
    private final Map<UUID, Integer> pendingExperience = new HashMap<>();
    private final Map<UUID, Integer> currentExperience = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {

        if (DefaultConfig.blacklistedWorlds.contains(event.getEntity().getWorld().getName())) return;
        if (!event.getEntity().hasPermission("resurrectionchest.use")) return;
        if (event.getKeepInventory()) return;

        Player player = event.getEntity();

        ResurrectionChestObject resurrectionChestObject = ResurrectionChestObject.getResurrectionChest(player);
        if (resurrectionChestObject == null || resurrectionChestObject.getLocation() == null || resurrectionChestObject.getLocation().getWorld() == null)
            return;

        Location chestLocation = resurrectionChestObject.getLocation();

        // Ensure the chunk containing the death chest is loaded before accessing the block.
        // If the player dies far from their chest the chunk may be unloaded, which can cause
        // the block type check to fail and incorrectly unregister the chest.
        if (!chestLocation.getWorld().isChunkLoaded(chestLocation.getBlockX() >> 4, chestLocation.getBlockZ() >> 4)) {
            chestLocation.getWorld().getChunkAt(chestLocation);
        }

        Block deathChestBlock = chestLocation.getBlock();

        if (!resurrectionChestObject.hasUsableTrackedChest()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', DefaultConfig.chestMissingMessage));
            resurrectionChestObject.remove();
            return;
        }

        Chest deathChest = (Chest) deathChestBlock.getState();

        if (DefaultConfig.enableDurabilityLossOnDeath)
            applyArmorDurabilityPenalty(
                    event.getDrops(),
                    player.getInventory().getArmorContents(),
                    DefaultConfig.durabilityToLower);

        List<ItemStack> overflowList = new ArrayList<>();

        for (ItemStack itemStack : event.getDrops()) {
            if (itemStack == null || itemStack.getType().isAir()) continue;
            Map<Integer, ItemStack> remainders =
                    deathChest.getInventory().addItem(itemStack.clone());
            for (ItemStack remainder : remainders.values()) {
                if (remainder != null && !remainder.getType().isAir())
                    overflowList.add(remainder.clone());
            }
        }

        event.getDrops().clear();
        if (!overflowList.isEmpty()) {
            event.getDrops().addAll(overflowList);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYour DeathChest was full! Some items were left where you died..."));
        }

        if (DefaultConfig.storeXP) {
            if (!event.getKeepLevel()) {
                UUID playerUuid = player.getUniqueId();
                int playerExperience = getCurrentExperience(player);
                double retainedRatio = DefaultConfig.xpPercentage;
                int retainedExperience =
                        clampExperience(Math.round(playerExperience * retainedRatio));
                pendingExperience.merge(playerUuid, retainedExperience, DeathEvent::addExperience);
                currentExperience.put(playerUuid, 0);
                event.setDroppedExp(0);
            }
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', DefaultConfig.deathMessage));

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChestOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        UUID playerUuid = player.getUniqueId();
        Integer experience = pendingExperience.get(playerUuid);
        if (!DefaultConfig.storeXP || experience == null) return;
        ResurrectionChestObject resurrectionChest = ResurrectionChestObject.getResurrectionChest(player);
        if (resurrectionChest == null || resurrectionChest.getLocation() == null) return;
        if (!resurrectionChest.hasUsableTrackedChest()) return;
        if (!isResurrectionChestInventory(event.getInventory(), resurrectionChest.getLocation())) return;

        player.giveExp(experience);
        currentExperience.put(playerUuid, Math.max(0, player.getTotalExperience()));
        pendingExperience.remove(playerUuid);
    }

    private boolean isResurrectionChestInventory(Inventory openedInventory, Location resurrectionChestLocation) {
        if (openedInventory == null || resurrectionChestLocation == null ||
                resurrectionChestLocation.getWorld() == null)
            return false;
        InventoryHolder holder = openedInventory.getHolder();
        if (holder instanceof Chest openedChest)
            return sameBlock(
                    openedChest.getLocation(),
                    resurrectionChestLocation);
        if (!(holder instanceof DoubleChest doubleChest))
            return false;
        return holderMatchesLocation(
                doubleChest.getLeftSide(),
                resurrectionChestLocation) ||
                holderMatchesLocation(
                        doubleChest.getRightSide(),
                        resurrectionChestLocation);
    }

    private boolean holderMatchesLocation(
            InventoryHolder holder,
            Location resurrectionChestLocation) {
        return holder instanceof Chest chest &&
                sameBlock(chest.getLocation(), resurrectionChestLocation);
    }

    private boolean sameBlock(Location first, Location second) {
        return first != null && second != null &&
                first.getWorld() != null && second.getWorld() != null &&
                first.getWorld().getUID().equals(second.getWorld().getUID()) &&
                first.getBlockX() == second.getBlockX() &&
                first.getBlockY() == second.getBlockY() &&
                first.getBlockZ() == second.getBlockZ();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(PlayerJoinEvent event) {
        currentExperience.put(event.getPlayer().getUniqueId(), Math.max(0, event.getPlayer().getTotalExperience()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExpChange(PlayerExpChangeEvent event) {
        if (event.getPlayer().isDead()) return;
        // Minecraft fires this event before applying an orb's final (possibly plugin-adjusted)
        // amount. Track the projected value so a later death sees the XP that was actually awarded.
        long projectedExperience = (long) event.getPlayer().getTotalExperience() + event.getAmount();
        currentExperience.put(event.getPlayer().getUniqueId(), clampExperience(projectedExperience));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        currentExperience.remove(event.getPlayer().getUniqueId());
    }

    private int getCurrentExperience(Player player) {
        UUID playerUuid = player.getUniqueId();
        int observedExperience = Math.max(0, player.getTotalExperience());
        Integer trackedExperience = currentExperience.get(playerUuid);

        // Direct API changes do not necessarily emit PlayerExpChangeEvent, so the live
        // player state wins whenever it has diverged from the event-driven ledger.
        if (trackedExperience == null || trackedExperience != observedExperience)
            currentExperience.put(playerUuid, observedExperience);

        return observedExperience;
    }

    private static int addExperience(int first, int second) {
        return clampExperience((long) first + second);
    }

    private static int clampExperience(long experience) {
        if (experience <= 0) return 0;
        return (int) Math.min(Integer.MAX_VALUE, experience);
    }

    private static void applyArmorDurabilityPenalty(List<ItemStack> drops,
                                                    ItemStack[] wornArmor,
                                                    int durabilityPenalty) {
        if (durabilityPenalty <= 0 || drops.isEmpty() || wornArmor.length == 0)
            return;

        for (ItemStack wornItem : wornArmor) {
            if (wornItem == null || wornItem.getType().isAir()) continue;

            int dropIndex = findWornDropIndex(drops, wornItem);
            if (dropIndex < 0) continue;

            ItemStack droppedItem = drops.get(dropIndex);
            if (!(droppedItem.getItemMeta() instanceof Damageable damageable))
                continue;

            long damaged = (long) damageable.getDamage() +
                    durabilityPenalty;
            int maximumDurability =
                    droppedItem.getType().getMaxDurability();
            if (maximumDurability <= 0) continue;
            if (damaged >= maximumDurability) {
                drops.remove(dropIndex);
            } else {
                damageable.setDamage((int) damaged);
                droppedItem.setItemMeta(damageable);
            }
        }
    }

    private static int findWornDropIndex(List<ItemStack> drops,
                                         ItemStack wornItem) {
        for (int index = 0; index < drops.size(); index++) {
            ItemStack droppedItem = drops.get(index);
            if (droppedItem != null && droppedItem.isSimilar(wornItem))
                return index;
        }

        return -1;
    }

}
