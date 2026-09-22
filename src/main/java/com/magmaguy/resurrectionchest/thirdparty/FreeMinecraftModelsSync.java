package com.magmaguy.resurrectionchest.thirdparty;

import com.magmaguy.magmacore.MagmaCore;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.resurrectionchest.MetadataHandler;
import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public final class FreeMinecraftModelsSync {
    private static boolean listening;
    private static BukkitTask refreshTask;

    private FreeMinecraftModelsSync() {}

    public static void initialize() {
        if (listening || !CustomModel.FMMIsEnabled()) return;
        // Keep the optional FMM event class out of listeners loaded without FMM.
        Bukkit.getPluginManager().registerEvents(new ReloadCompletionListener(), MetadataHandler.PLUGIN);
        listening = true;
    }

    public static void shutdown() {
        if (refreshTask != null) refreshTask.cancel();
        refreshTask = null;
        listening = false;
    }

    public static void reloadAndRefreshModels(CommandSender sender) {
        if (!CustomModel.FMMIsEnabled()) {
            if (sender != null) Logger.sendMessage(sender, "&eFreeMinecraftModels is not installed. ResurrectionChest model files were updated on disk, but custom chest props will stay disabled until FreeMinecraftModels is installed.");
            ResurrectionChestObject.refreshAllModels();
            return;
        }
        initialize();
        // The completion event, including externally initiated reloads, owns refresh.
        com.magmaguy.freeminecraftmodels.commands.ReloadCommand.reloadPlugin(
                sender != null ? sender : Bukkit.getConsoleSender());
    }

    public static void refreshModelsWhenReady() {
        initialize();
        if (!CustomModel.FMMIsEnabled() || MagmaCore.isPluginReady("FreeMinecraftModels")) queueRefresh();
    }

    private static void queueRefresh() {
        if (refreshTask != null) return;
        refreshTask = Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            refreshTask = null;
            if (!CustomModel.FMMIsEnabled() || MagmaCore.isPluginReady("FreeMinecraftModels"))
                ResurrectionChestObject.refreshAllModels();
        });
    }

    public static final class ReloadCompletionListener implements Listener {
        @EventHandler
        public void onReloaded(com.magmaguy.freeminecraftmodels.api.FmmReloadedEvent event) {
            queueRefresh();
        }
    }
}
