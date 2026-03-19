package com.magmaguy.resurrectionchest.thirdparty;

import com.magmaguy.magmacore.MagmaCore;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.resurrectionchest.MetadataHandler;
import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public final class FreeMinecraftModelsSync {
    private static final int MAX_ATTEMPTS = 120;

    private FreeMinecraftModelsSync() {
    }

    public static void reloadAndRefreshModels(CommandSender sender) {
        if (!CustomModel.FMMIsEnabled()) {
            if (sender != null) {
                Logger.sendMessage(sender, "&eFreeMinecraftModels is not installed. ResurrectionChest model files were updated on disk, but custom chest props will stay disabled until FreeMinecraftModels is installed.");
            }
            ResurrectionChestObject.refreshAllModels();
            return;
        }

        com.magmaguy.freeminecraftmodels.commands.ReloadCommand.reloadPlugin(
                sender != null ? sender : Bukkit.getConsoleSender());
        refreshModelsWhenReady();
    }

    public static void refreshModelsWhenReady() {
        if (!CustomModel.FMMIsEnabled()) {
            ResurrectionChestObject.refreshAllModels();
            return;
        }

        new BukkitRunnable() {
            int attempts = 0;

            @Override
            public void run() {
                attempts++;
                if (MagmaCore.isPluginReady("FreeMinecraftModels")) {
                    ResurrectionChestObject.refreshAllModels();
                    cancel();
                    return;
                }
                if (attempts >= MAX_ATTEMPTS) {
                    Logger.warn("Timed out waiting for FreeMinecraftModels to finish reloading. Refreshing ResurrectionChest models anyway.");
                    ResurrectionChestObject.refreshAllModels();
                    cancel();
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 10L, 10L);
    }
}
