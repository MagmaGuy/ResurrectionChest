package com.magmaguy.resurrectionchest.events;

import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeathEvent implements Listener {
    HashMap<Player, Float> xp = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {

        if (DefaultConfig.blacklistedWorlds.contains(event.getEntity().getWorld().getName())) return;
        if (!event.getEntity().hasPermission("resurrectionchest.use")) return;
        if (event.getKeepInventory()) return;

        Player player = event.getEntity();

        ResurrectionChestObject resurrectionChestObject = ResurrectionChestObject.getResurrectionChest(player);
        if (resurrectionChestObject == null) return;

        Block deathChestBlock = resurrectionChestObject.getLocation().getBlock();

        if (!deathChestBlock.getType().equals(Material.CHEST)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', DefaultConfig.chestMissingMessage));
            DeathChestRemover.unregisterDeathChestEntry(resurrectionChestObject.getLocation());
            return;
        }

        Chest deathChest = (Chest) deathChestBlock.getState();

        List<ItemStack> dropList = event.getDrops();
        List<ItemStack> overflowList = new ArrayList<>();

        for (ItemStack itemStack : dropList)
            if (itemStack != null)
                if (deathChest.getInventory().firstEmpty() > -1)
                    deathChest.getInventory().addItem(itemStack);
                else overflowList.add(itemStack);

        event.getDrops().clear();
        if (!overflowList.isEmpty()) {
            event.getDrops().addAll(overflowList);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYour DeathChest was full! Some items were left where you died..."));
        }

        if (DefaultConfig.storeXP) {
            xp.put(player, (float) (event.getDroppedExp() * DefaultConfig.xpPercentage));
            event.setDroppedExp(0);
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', DefaultConfig.deathMessage));

    }

    @EventHandler(ignoreCancelled = true)
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getClickedBlock().getType() != Material.CHEST &&
                event.getClickedBlock().getType() != Material.TRAPPED_CHEST &&
                event.getClickedBlock().getType() != Material.SHULKER_BOX)
            return;
        if (!DefaultConfig.storeXP || !xp.containsKey(event.getPlayer())) return;
        if (!ResurrectionChestObject.getResurrectionChest(event.getPlayer()).getLocation().getBlock().equals(event.getClickedBlock()))
            return;
        int exp = Math.round(xp.get(event.getPlayer()));
        event.getPlayer().giveExp(exp);
        xp.remove(event.getPlayer());
    }
}
