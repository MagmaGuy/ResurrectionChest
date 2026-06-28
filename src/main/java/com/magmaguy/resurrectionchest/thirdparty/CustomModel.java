package com.magmaguy.resurrectionchest.thirdparty;

import com.magmaguy.freeminecraftmodels.MetadataHandler;
import com.magmaguy.freeminecraftmodels.api.ModeledEntityManager;
import com.magmaguy.freeminecraftmodels.config.props.PropBlocks;
import com.magmaguy.freeminecraftmodels.customentity.PropEntity;
import com.magmaguy.freeminecraftmodels.dataconverter.FileModelConverter;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.resurrectionchest.ResurrectionChest;
import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomModel {
    public final static NamespacedKey RESURRECTIONCHEST_OWNER = new NamespacedKey(ResurrectionChest.plugin, "resurrectionchest_owner");
    public final ResurrectionChestObject resurrectionChestObject;
    private final PropEntity propEntity;

    private CustomModel(Location location, UUID uuid, ResurrectionChestObject resurrectionChestObject, PropEntity propEntity) {
        this.resurrectionChestObject = resurrectionChestObject;
        Player player = Bukkit.getPlayer(resurrectionChestObject.getUuid());

        this.propEntity = propEntity;
        propEntity.setCustomDataString(RESURRECTIONCHEST_OWNER, uuid.toString());
        propEntity.setPersistent(false);

        if (player != null)
            propEntity.setDisplayName(DefaultConfig.deathChestNameTag.replace("$playerName", player.getDisplayName()));
        else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(resurrectionChestObject.getUuid());
            if (offlinePlayer.getName() != null)
                propEntity.setDisplayName(DefaultConfig.deathChestNameTag.replace("$playerName", offlinePlayer.getName()));
        }

        List<PropBlocks> propBlocks = new ArrayList<>();
        for (Location chestLocation : resurrectionChestObject.getAllChests()) {
            propBlocks.add(new PropBlocks(chestLocation, Material.BARRIER));
        }
        for (Location signLocation : resurrectionChestObject.getAllSigns()) {
            propBlocks.add(new PropBlocks(signLocation, Material.AIR));
        }
        propEntity.setPropBlocks(propBlocks);
        setupResurrectionChestCallbacks(uuid, location);
    }

    public static boolean FMMIsEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels");
    }

    public static CustomModel CreateChestProp(Location location, UUID uuid, ResurrectionChestObject resurrectionChestObject, String modelName) {
        if (!FMMIsEnabled() || FileModelConverter.getConvertedFileModels().get(modelName) == null) return null;
        PropEntity propEntity = findLoadedProp(modelName, location);
        if (propEntity == null) {
            propEntity = PropEntity.spawnPropEntity(modelName, location);
        }
        if (propEntity == null) {
            Logger.warn("[ResurrectionChest] FreeMinecraftModels refused to create chest model '" + modelName
                    + "' at " + formatBlockLocation(location) + ". A duplicate prop is probably already present.");
            return null;
        }
        return new CustomModel(location, uuid, resurrectionChestObject, propEntity);
    }

    private static PropEntity findLoadedProp(String modelName, Location location) {
        if (modelName == null || location == null || location.getWorld() == null) return null;

        for (PropEntity propEntity : ModeledEntityManager.propEntities().values()) {
            if (propEntity == null || propEntity.getUnderlyingEntity() == null) continue;
            if (!modelName.equals(propEntity.getEntityID())) continue;
            if (!sameBlock(location, propEntity.getUnderlyingEntity().getLocation())) continue;
            return propEntity;
        }

        return null;
    }

    private static boolean sameBlock(Location first, Location second) {
        if (first == null || second == null || first.getWorld() == null || second.getWorld() == null) return false;
        return first.getWorld().getUID().equals(second.getWorld().getUID())
                && first.getBlockX() == second.getBlockX()
                && first.getBlockY() == second.getBlockY()
                && first.getBlockZ() == second.getBlockZ();
    }

    private static String formatBlockLocation(Location location) {
        if (location == null || location.getWorld() == null) return "unknown location";
        return location.getWorld().getName() + " "
                + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private void setupResurrectionChestCallbacks(UUID ownerUuid, Location chestLocation) {
        propEntity
                // Left click callback - destroy the chest
                .setLeftClickCallback((player, entity) -> {
                    // Check if player owns this chest
                    if (!ownerUuid.equals(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "You can only destroy your own resurrection chest!");
                        return;
                    }

                    // Play destruction sound
                    player.playSound(chestLocation, Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);

                    resurrectionChestObject.remove();
                })
                // Right click callback - open the chest
                .setRightClickCallback((player, entity) -> {
                    // Check if there's still a chest block at the location
                    if (chestLocation.getBlock().getType() != Material.CHEST) {
                        player.sendMessage(ChatColor.RED + "The chest block is missing!");
                        return;
                    }

                    // Get the chest tile entity and open it
                    Chest chestBlock = (Chest) chestLocation.getBlock().getState();
                    Inventory chestInventory = chestBlock.getInventory();

                    // Play chest open sound
                    player.playSound(chestLocation, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);

                    // Open the chest inventory
                    player.openInventory(chestInventory);

                    new BukkitRunnable() {
                        int counter = 0;

                        @Override
                        public void run() {
                            propEntity.showFakePropBlocksToAllPlayers();
                            counter++;
                            if (counter > 1) {
                                cancel();
                            }
                        }
                    }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
                });
    }

    public void remove() {
        if (propEntity != null) propEntity.permanentlyRemove();
    }

    public void refreshPropBlocks() {
        if (propEntity != null) propEntity.showFakePropBlocksToAllPlayers();
    }

}
