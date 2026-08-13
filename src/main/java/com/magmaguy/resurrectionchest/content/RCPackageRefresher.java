package com.magmaguy.resurrectionchest.content;

import com.magmaguy.magmacore.nightbreak.NightbreakContentRefresher;
import com.magmaguy.resurrectionchest.MetadataHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public final class RCPackageRefresher {
    private static final Duration REFRESH_COOLDOWN = Duration.ofMinutes(5);
    private static final String CATALOG_KEY = "nightbreak-packages";

    private RCPackageRefresher() {
    }

    public static void refreshContentAndAccess() {
        NightbreakContentRefresher.refreshAsyncIfDue(
                (JavaPlugin) MetadataHandler.PLUGIN,
                CATALOG_KEY,
                REFRESH_COOLDOWN,
                () -> RCPackage.getRcPackages().values(),
                rcPackage -> true,
                outdated -> {
                });
    }

    public static void reset() {
        NightbreakContentRefresher.resetRefreshCooldown(
                (JavaPlugin) MetadataHandler.PLUGIN, CATALOG_KEY);
    }
}
