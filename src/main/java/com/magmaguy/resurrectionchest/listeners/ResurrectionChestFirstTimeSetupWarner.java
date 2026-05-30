package com.magmaguy.resurrectionchest.listeners;

import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ResurrectionChestFirstTimeSetupWarner implements Listener {
    private final JavaPlugin ownerPlugin;

    public ResurrectionChestFirstTimeSetupWarner(JavaPlugin ownerPlugin) {
        this.ownerPlugin = ownerPlugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (DefaultConfig.isSetupDone()) return;
        if (!event.getPlayer().hasPermission("resurrectionchest.*")) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!event.getPlayer().isOnline()) return;
                if (DefaultConfig.isSetupDone()) return;

                Logger.sendSimpleMessage(event.getPlayer(), "&8&m----------------------------------------------------");
                Logger.sendMessage(event.getPlayer(), "&fFirst boot message:");
                Logger.sendSimpleMessage(event.getPlayer(), "&7It looks like this is your first boot. &aResurrectionChest is ready to use.");
                event.getPlayer().spigot().sendMessage(
                        SpigotMessage.commandHoverMessage("&a/resurrectionchest setup",
                                "&7Browse and install premade ResurrectionChest content.",
                                "/resurrectionchest setup"),
                        SpigotMessage.simpleMessage("&7 opens premade content and permanently dismisses this message."));
                Logger.sendSimpleMessage(event.getPlayer(), "&7To make a resurrection chest, place a chest and put a sign that says &a[deathchest]&7 on it.");
                Logger.sendSimpleMessage(event.getPlayer(), "&8&m----------------------------------------------------");
            }
        }.runTaskLater(ownerPlugin, 20L * 10);
    }
}
