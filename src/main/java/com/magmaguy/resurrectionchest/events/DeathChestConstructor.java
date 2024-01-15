package com.magmaguy.resurrectionchest.events;

import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import com.magmaguy.resurrectionchest.configs.PlayerDataConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class DeathChestConstructor implements Listener {

    @EventHandler
    public void onSignPlace(SignChangeEvent event) {

        if (!event.getPlayer().hasPermission("resurrectionchest.use")) return;
        if (DefaultConfig.blacklistedWorlds.contains(event.getBlock().getLocation().getWorld().getName())) return;
        if (!event.getLine(0).equalsIgnoreCase(DefaultConfig.resurrectionChestSignName) &&
                !event.getLine(1).equalsIgnoreCase(DefaultConfig.resurrectionChestSignName))
            return;
        if (!(event.getBlock().getBlockData() instanceof WallSign))
            return;

        WallSign sign = (WallSign) event.getBlock().getBlockData();
        Block attached = event.getBlock().getRelative(sign.getFacing().getOppositeFace());
        if (attached.getType() != Material.CHEST) return;

        if (ResurrectionChestObject.getResurrectionChest(event.getPlayer()) != null)
            ResurrectionChestObject.getResurrectionChest(event.getPlayer()).remove();
        ResurrectionChestObject resurrectionChest = new ResurrectionChestObject(event.getPlayer().getUniqueId(), attached.getLocation());
        PlayerDataConfig.addPlayerdata(event.getPlayer().getUniqueId(), attached.getLocation());

        event.setLine(0, "");
        event.setLine(1, ChatColor.translateAlternateColorCodes('&', "&5" + DefaultConfig.resurrectionChestSignName));
        event.setLine(2, event.getPlayer().getDisplayName());
        event.setLine(3, "");

        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', DefaultConfig.chestCreationMessage));

        resurrectionChest.initializeParticleEffects();

    }

}
