package com.magmaguy.resurrectionchest.listeners;

import com.magmaguy.magmacore.events.ModelInstallationEvent;
import com.magmaguy.resurrectionchest.thirdparty.FreeMinecraftModelsSync;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ModelInstallationListener implements Listener {
    @EventHandler
    public void onPluginEnable(org.bukkit.event.server.PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("FreeMinecraftModels")) FreeMinecraftModelsSync.initialize();
    }

    @EventHandler
    public void onModelInstallation(ModelInstallationEvent event) {
        FreeMinecraftModelsSync.refreshModelsWhenReady();
    }
}
