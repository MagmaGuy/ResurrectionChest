package com.magmaguy.resurrectionchest;

import com.magmaguy.magmacore.MagmaCore;
import com.magmaguy.magmacore.command.CommandManager;
import com.magmaguy.magmacore.initialization.PluginInitializationConfig;
import com.magmaguy.magmacore.initialization.PluginInitializationContext;
import com.magmaguy.magmacore.initialization.PluginInitializationState;
import com.magmaguy.magmacore.nightbreak.NightbreakFirstTimeSetupSpec;
import com.magmaguy.magmacore.nightbreak.NightbreakFirstTimeSetupWarner;
import com.magmaguy.magmacore.nightbreak.NightbreakPluginBootstrap;
import com.magmaguy.magmacore.nightbreak.NightbreakPluginHooks;
import com.magmaguy.magmacore.nightbreak.NightbreakPluginSpec;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.resurrectionchest.commands.*;
import com.magmaguy.resurrectionchest.config.contentpackages.ContentPackageConfig;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import com.magmaguy.resurrectionchest.configs.PlayerDataConfig;
import com.magmaguy.resurrectionchest.content.RCPackage;
import com.magmaguy.resurrectionchest.content.RCPackageRefresher;
import com.magmaguy.resurrectionchest.events.DeathChestConstructor;
import com.magmaguy.resurrectionchest.events.DeathChestRemover;
import com.magmaguy.resurrectionchest.events.DeathEvent;
import com.magmaguy.resurrectionchest.listeners.ModelInstallationListener;
import com.magmaguy.resurrectionchest.menus.ResurrectionChestFirstTimeSetupMenu;
import com.magmaguy.resurrectionchest.menus.ResurrectionChestSetupMenu;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ResurrectionChest extends JavaPlugin {
    public static final NightbreakPluginSpec NIGHTBREAK_PLUGIN_SPEC = new NightbreakPluginSpec(
            "ResurrectionChest",
            "resurrectionchest",
            "resurrectionchest.*",
            "resurrectionchest.setup",
            "resurrectionchest.initialize",
            "https://nightbreak.io/plugin/resurrectionchest/",
            "Reloaded ResurrectionChest.",
            true, false, true);
    public static final NightbreakFirstTimeSetupSpec FIRST_TIME_SETUP_SPEC = new NightbreakFirstTimeSetupSpec(
            "ResurrectionChest",
            "resurrectionchest.*",
            null,
            "/resurrectionchest setup",
            "/resurrectionchest downloadall",
            "https://nightbreak.io/plugin/resurrectionchest/",
            "",
            java.util.List.of("&7ResurrectionChest already works with vanilla chests. Custom chest props are optional and use FreeMinecraftModels."),
            java.util.List.of(
                    "&7The plugin already works with vanilla death chests.",
                    "&7Nightbreak content is optional and adds custom chest props when FreeMinecraftModels is installed.",
                    "&eInstall FreeMinecraftModels if you want those custom chest props to display in-game."));

    public static Plugin plugin = null;

    @Override
    public void onEnable() {
        MetadataHandler.PLUGIN = plugin = this;
        Bukkit.getLogger().info("\n" +
                "                                                                       \n" +
                " _____                             _   _         _____ _           _   \n" +
                "| __  |___ ___ _ _ ___ ___ ___ ___| |_|_|___ ___|     | |_ ___ ___| |_ \n" +
                "|    -| -_|_ -| | |  _|  _| -_|  _|  _| | . |   |   --|   | -_|_ -|  _|\n" +
                "|__|__|___|___|___|_| |_| |___|___|_| |_|___|_|_|_____|_|_|___|___|_|  \n" +
                "                                                                       \n");
        Bukkit.getLogger().info("[ResurrectionChest] Initialized version " + this.getDescription().getVersion() + "!");
        NightbreakPluginBootstrap.startInitialization(this,
                new PluginInitializationConfig("ResurrectionChest", "resurrectionchest.*", 8),
                NIGHTBREAK_PLUGIN_SPEC,
                new NightbreakPluginHooks() {
                    @Override
                    public void asyncInitialization(PluginInitializationContext initializationContext) {
                        ResurrectionChest.this.asyncInitialization(initializationContext);
                    }

                    @Override
                    public void syncInitialization(PluginInitializationContext initializationContext) {
                        ResurrectionChest.this.syncInitialization(initializationContext);
                    }

                    @Override
                    public void onInitializationSuccess() {
                        Logger.info("ResurrectionChest fully initialized!");
                    }

                    @Override
                    public void onInitializationFailure(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    public void onLoad() {
        MagmaCore.createInstance(this);
    }

    @Override
    public void onDisable() {
        MagmaCore.requestInitializationShutdown(this);
        if (MagmaCore.getInitializationState(this.getName()) == PluginInitializationState.INITIALIZING) {
            Bukkit.getServer().getScheduler().cancelTasks(MetadataHandler.PLUGIN);
            MagmaCore.shutdown(this);
            Bukkit.getLogger().info("[ResurrectionChest] Shutdown during initialization.");
            return;
        }

        Bukkit.getServer().getScheduler().cancelTasks(MetadataHandler.PLUGIN);
        MagmaCore.shutdown(this);
        HandlerList.unregisterAll(MetadataHandler.PLUGIN);
        RCPackage.shutdown();
        RCPackageRefresher.reset();
        PersistentObjectHandler.shutdown();
        ResurrectionChestObject.shutdown();
        ResurrectionChestObject.getResurrectionChests().values().forEach(ResurrectionChestObject::chunkUnload);
        ResurrectionChestObject.getResurrectionChests().clear();
        Bukkit.getLogger().info("[ResurrectionChest] Shutdown!");
    }

    private void asyncInitialization(PluginInitializationContext initializationContext) {
        initializationContext.step("Base Configs");
        new DefaultConfig();
        new PlayerDataConfig();

        initializationContext.step("Content Importer");
        MagmaCore.initializeImporter(this);

        initializationContext.step("Content Packages");
        new ContentPackageConfig();
    }

    private void syncInitialization(PluginInitializationContext initializationContext) {
        initializationContext.step("Event Listeners");
        this.getServer().getPluginManager().registerEvents(new DeathChestConstructor(), this);
        this.getServer().getPluginManager().registerEvents(new DeathChestRemover(), this);
        this.getServer().getPluginManager().registerEvents(new DeathEvent(), this);
        this.getServer().getPluginManager().registerEvents(new PersistentObjectHandler.PersistentObjectHandlerEvents(), this);
        this.getServer().getPluginManager().registerEvents(new NightbreakFirstTimeSetupWarner(this, FIRST_TIME_SETUP_SPEC, DefaultConfig::isSetupDone), this);
        this.getServer().getPluginManager().registerEvents(new ModelInstallationListener(), this);

        initializationContext.step("Commands");
        CommandManager commandManager = new CommandManager(this, "resurrectionchest");
        commandManager.registerCommand(new ReloadCommand());
        commandManager.registerCommand(new ResurrectionChestCommand());
        NightbreakPluginBootstrap.registerStandardCommands(this,
                commandManager,
                NIGHTBREAK_PLUGIN_SPEC,
                ResurrectionChestSetupMenu::createMenu,
                ResurrectionChestFirstTimeSetupMenu::createMenu,
                () -> new java.util.ArrayList<>(RCPackage.getRcPackages().values()),
                ReloadCommand::reload);

        initializationContext.step("Death Chests");
        ResurrectionChestObject.initializeConfigDeathchests();
        ResurrectionChestObject.startClock();

        initializationContext.step("Version Check");
        MagmaCore.checkVersionUpdate("57541", "https://nightbreak.io/plugin/resurrectionchest/");

        initializationContext.step("Metrics");
        new Metrics(this, 2677);
    }
}
