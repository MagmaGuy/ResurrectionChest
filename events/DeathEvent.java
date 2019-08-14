package com.magmaguy.resurrectionchest.events;

import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import org.bukkit.ChatColor;
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

        if (DefaultConfig.blacklistedWorlds.contains(event.getEntity().getWorld().getName())) return;
        if (!event.getEntity().hasPermission("resurrectionchest.use")) return;
        if (event.getKeepInventory()) return;

        Player player = event.getEntity();

        ResurrectionChestObject resurrectionChestObject = ResurrectionChestObject.getResurrectionChest(player);
        if (resurrectionChestObject == null) return;

        if (player.getInventory().contains(Material.TOTEM_OF_UNDYING)) return;

        Block deathChestBlock = resurrectionChestObject.getLocation().getBlock();

        if (!deathChestBlock.getType().equals(Material.CHEST)) {

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', DefaultConfig.chestMissingMessage));
            DeathChestRemover.unregisterDeathChestEntry(resurrectionChestObject.getLocation());
            return;

        }

        Chest deathChest = (Chest) deathChestBlock.getState();

        List<ItemStack> dropList = event.getDrops();

        if (DefaultConfig.enableDurabilityLossOnDeath)
            dropList = damageArmorDurability(player, event.getDrops());

        for (ItemStack itemStack : dropList)
            if (itemStack != null)
                deathChest.getInventory().addItem(itemStack);

        event.getDrops().clear();

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', DefaultConfig.deathMessage));

    }

    private List<ItemStack> damageArmorDurability(Player player, List<ItemStack> dropList) {

        boolean noArmor = true;

        for (ItemStack itemStack : player.getInventory().getArmorContents())
            if (itemStack != null)
                noArmor = false;

        if (noArmor) return dropList;

        for (ItemStack primaryItemStack : dropList)
            for (ItemStack itemStack : player.getInventory().getArmorContents())
                if (primaryItemStack.equals(itemStack))
                    primaryItemStack.setDurability((short) (primaryItemStack.getDurability() + DefaultConfig.durabilityToLower));

        return dropList;

    }

}
