package com.magmaguy.resurrectionchest;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DeathEvent implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {

        if (!event.getEntity().hasPermission("resurrectionchest.use")) return;
        if (event.getKeepInventory()) return;
        if (!ConfigValues.playerDataConfig.contains(event.getEntity().getUniqueId().toString())) return;

        Player player = event.getEntity();

        if (player.getInventory().contains(Material.TOTEM)) return;

        Location blockLocation = LocationParser.parseLocation(ConfigValues.playerDataConfig.getString(player.getUniqueId().toString()));
        if (blockLocation == null) return;

        Block deathChestBlock = blockLocation.getBlock();

        if (!deathChestBlock.getType().equals(Material.CHEST)) {

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigValues.defaultConfig.getString(DefaultConfig.CHEST_MISSING_MESSAGE)));
            DeathChestRemover.unregisterDeathChestEntry(blockLocation);
            return;

        }

        Chest deathChest = (Chest) deathChestBlock.getState();

        List<ItemStack> dropList = event.getDrops();

        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.ENABLE_DURABILITY_LOSS_ON_DEATH))
            dropList = damageArmorDurability(player, event.getDrops());

        for (ItemStack itemStack : dropList) {

            if (itemStack != null) {

                deathChest.getInventory().addItem(itemStack);

            }

        }

        event.getDrops().clear();

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigValues.defaultConfig.getString(DefaultConfig.DEATH_MESSAGE)));

    }

    private List<ItemStack> damageArmorDurability(Player player, List<ItemStack> dropList) {

        boolean noArmor = true;

        for (ItemStack itemStack : player.getInventory().getArmorContents()) {
            if (itemStack != null)
                noArmor = false;
        }

        if (noArmor) return dropList;

        for (ItemStack primaryItemStack : dropList) {

            for (ItemStack itemStack : player.getInventory().getArmorContents()) {

                if (primaryItemStack.equals(itemStack)) {

                    primaryItemStack.setDurability((short) (primaryItemStack.getDurability() + ConfigValues.defaultConfig.getDouble(DefaultConfig.DURABILITY_TO_LOWER)));

                }

            }

        }

        return dropList;

    }

}
