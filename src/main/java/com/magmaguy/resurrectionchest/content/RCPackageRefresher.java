package com.magmaguy.resurrectionchest.content;

import com.magmaguy.magmacore.nightbreak.NightbreakContentRefresher;
import com.magmaguy.resurrectionchest.MetadataHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class RCPackageRefresher {
    private static final long REFRESH_COOLDOWN_MS = 5 * 60 * 1000L;
    private static long lastRefresh = 0L;

    private RCPackageRefresher() {
    }

    public static void refreshContentAndAccess() {
        long now = System.currentTimeMillis();
        if (now - lastRefresh < REFRESH_COOLDOWN_MS) return;
        lastRefresh = now;
        NightbreakContentRefresher.refreshAsync(
                (JavaPlugin) MetadataHandler.PLUGIN,
                new ArrayList<>(RCPackage.getRcPackages().values()),
                rcPackage -> true,
                outdated -> {
                });
    }

    public static void reset() {
        lastRefresh = 0L;
    }
}
