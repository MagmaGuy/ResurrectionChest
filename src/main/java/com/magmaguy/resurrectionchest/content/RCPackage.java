package com.magmaguy.resurrectionchest.content;

import com.magmaguy.magmacore.nightbreak.AbstractNightbreakContentPackage;
import com.magmaguy.magmacore.nightbreak.NightbreakFileUtils;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.resurrectionchest.MetadataHandler;
import com.magmaguy.resurrectionchest.commands.ReloadCommand;
import com.magmaguy.resurrectionchest.config.contentpackages.ContentPackageConfigFields;
import com.magmaguy.resurrectionchest.thirdparty.FreeMinecraftModelsSync;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RCPackage extends AbstractNightbreakContentPackage {
    @Getter
    private static final Map<String, RCPackage> rcPackages = new HashMap<>();
    @Getter
    private final ContentPackageConfigFields contentPackageConfigFields;

    public RCPackage(ContentPackageConfigFields contentPackageConfigFields) {
        this.contentPackageConfigFields = contentPackageConfigFields;
        rcPackages.put(contentPackageConfigFields.getFilename(), this);
    }

    public static void shutdown() {
        rcPackages.clear();
    }

    @Override
    protected void doInstall(Player player) {
        player.closeInventory();
        List<File> disabledEntries = collectManagedEntries(getDisabledModelsFolder());
        if (disabledEntries.isEmpty()) {
            Logger.sendMessage(player, "&cCould not find the disabled ResurrectionChest model files for " + getDisplayName());
            return;
        }

        NightbreakFileUtils.moveEntriesPreservingRelativePaths(disabledEntries, getDisabledModelsFolder(), getInstalledModelsFolder());
        handleStateSave(player,
                contentPackageConfigFields.setEnabledAndSave(true),
                () -> {
                    if (player.isOnline()) {
                        Logger.sendSimpleMessage(player, "&aReloading FreeMinecraftModels so ResurrectionChest can use " + getDisplayName() + "&a...");
                    }
                    FreeMinecraftModelsSync.reloadAndRefreshModels(player);
                },
                "&cFailed to update ResurrectionChest package state. Check the console.");
    }

    @Override
    protected void doUninstall(Player player) {
        player.closeInventory();
        List<File> installedEntries = collectManagedEntries(getInstalledModelsFolder());
        if (installedEntries.isEmpty()) {
            Logger.sendMessage(player, "&cCould not find the installed ResurrectionChest model files for " + getDisplayName());
            return;
        }

        NightbreakFileUtils.moveEntriesPreservingRelativePaths(installedEntries, getInstalledModelsFolder(), getDisabledModelsFolder());
        handleStateSave(player,
                contentPackageConfigFields.setEnabledAndSave(false),
                () -> {
                    if (player.isOnline()) {
                        Logger.sendSimpleMessage(player, "&aReloading FreeMinecraftModels so ResurrectionChest stops using " + getDisplayName() + "&a...");
                    }
                    FreeMinecraftModelsSync.reloadAndRefreshModels(player);
                },
                "&cFailed to update ResurrectionChest package state. Check the console.");
    }

    private File getInstalledModelsFolder() {
        File fmmFolder = getFreeMinecraftModelsFolder();
        File lowercaseFolder = new File(fmmFolder, "models");
        File uppercaseFolder = new File(fmmFolder, "Models");
        if (lowercaseFolder.exists() || !uppercaseFolder.exists()) {
            return lowercaseFolder;
        }
        return uppercaseFolder;
    }

    private File getDisabledModelsFolder() {
        File fmmFolder = getFreeMinecraftModelsFolder();
        File lowercaseFolder = new File(fmmFolder, "models_disabled");
        File uppercaseFolder = new File(fmmFolder, "Models_disabled");
        if (lowercaseFolder.exists() || !uppercaseFolder.exists()) {
            return lowercaseFolder;
        }
        return uppercaseFolder;
    }

    private File getFreeMinecraftModelsFolder() {
        return new File(MetadataHandler.PLUGIN.getDataFolder().getParentFile(), "FreeMinecraftModels");
    }

    private List<File> collectManagedEntries(File rootFolder) {
        return NightbreakFileUtils.collectRecursiveFiles(rootFolder,
                contentPackageConfigFields.getContentFilePrefixes());
    }

    @Override
    protected JavaPlugin getOwnerPlugin() {
        return (JavaPlugin) MetadataHandler.PLUGIN;
    }

    @Override
    protected String getPluginDisplayName() {
        return "ResurrectionChest";
    }

    @Override
    protected String getContentPageUrl() {
        return "https://nightbreak.io/plugin/resurrectionchest/";
    }

    @Override
    protected List<String> getPackageDescription() {
        return contentPackageConfigFields.getDescription();
    }

    @Override
    protected String getManualImportsFolderName() {
        return "ResurrectionChest imports";
    }

    @Override
    protected String getManualReloadCommand() {
        return "/resurrectionchest reload";
    }

    @Override
    protected void onDownloadStateSaved(Player player) {
        Logger.sendSimpleMessage(player, "&aReloading ResurrectionChest so the new content is imported...");
        ReloadCommand.reload(player);
    }

    @Override
    public String getNightbreakSlug() {
        return contentPackageConfigFields.getNightbreakSlug();
    }

    @Override
    public String getDisplayName() {
        return contentPackageConfigFields.getName();
    }

    @Override
    public String getDownloadLink() {
        return contentPackageConfigFields.getDownloadLink();
    }

    @Override
    public int getLocalVersion() {
        return contentPackageConfigFields.getVersion();
    }

    @Override
    public CompletableFuture<Void> enableAfterDownload() {
        return contentPackageConfigFields.setEnabledAndSave(true);
    }

    @Override
    public boolean isInstalled() {
        return contentPackageConfigFields.isEnabled() && !collectManagedEntries(getInstalledModelsFolder()).isEmpty();
    }

    @Override
    public boolean isDownloaded() {
        return !collectManagedEntries(getInstalledModelsFolder()).isEmpty()
                || !collectManagedEntries(getDisabledModelsFolder()).isEmpty();
    }
}
