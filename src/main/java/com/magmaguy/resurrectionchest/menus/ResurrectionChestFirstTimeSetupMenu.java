package com.magmaguy.resurrectionchest.menus;

import com.magmaguy.resurrectionchest.ResurrectionChest;
import com.magmaguy.magmacore.menus.FirstTimeSetupMenu;
import com.magmaguy.magmacore.menus.MenuButton;
import com.magmaguy.magmacore.nightbreak.NightbreakSetupMenuHelper;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import com.magmaguy.resurrectionchest.MetadataHandler;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import com.magmaguy.resurrectionchest.content.RCPackage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ResurrectionChestFirstTimeSetupMenu {
    private ResurrectionChestFirstTimeSetupMenu() {
    }

    public static void createMenu(Player player) {
        new FirstTimeSetupMenu(
                (JavaPlugin) MetadataHandler.PLUGIN,
                player,
                "&2ResurrectionChest",
                "&6Nightbreak-powered content setup",
                createInfoItem(),
                List.of(createRecommendedItem(), createManualItem(), createUseCurrentContentItem()));
    }

    private static MenuButton createInfoItem() {
        return new MenuButton(ItemStackGenerator.generateSkullItemStack(
                "magmaguy",
                "&2Welcome to ResurrectionChest!",
                List.of(
                        "&7The plugin works with vanilla death chests by default.",
                        "&7Nightbreak + FreeMinecraftModels can add optional custom chest props."))) {
            @Override
            public void onClick(Player player) {
                player.closeInventory();
                NightbreakSetupMenuHelper.sendFirstTimeSetupResources(player, ResurrectionChest.FIRST_TIME_SETUP_SPEC);
                if (Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
                    sendCommand(player, "&2FreeMinecraftModels setup: ", "&a/fmm setup",
                            "&7Click to open the FreeMinecraftModels setup menu.",
                            "/fmm setup");
                } else {
                    sendLink(player, "&2FreeMinecraftModels: ", "&9&nhttps://nightbreak.io/plugin/freeminecraftmodels/",
                            "&7Click to open the FreeMinecraftModels page.",
                            "https://nightbreak.io/plugin/freeminecraftmodels/");
                }
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
            }
        };
    }

    private static MenuButton createRecommendedItem() {
        return new MenuButton(ItemStackGenerator.generateItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&2Recommended Setup",
                List.of(
                        "&aMarks setup complete.",
                        "&aGuides you to Nightbreak login and ResurrectionChest content install."))) {
            @Override
            public void onClick(Player player) {
                player.closeInventory();
                DefaultConfig.toggleSetupDone(true);
                NightbreakSetupMenuHelper.sendRecommendedSetupInstructions(player, ResurrectionChest.FIRST_TIME_SETUP_SPEC);
            }
        };
    }

    private static MenuButton createManualItem() {
        return new MenuButton(ItemStackGenerator.generateItemStack(
                Material.YELLOW_STAINED_GLASS_PANE,
                "&6Manual Setup",
                List.of("&eMarks setup complete and leaves content management up to you."))) {
            @Override
            public void onClick(Player player) {
                player.closeInventory();
                DefaultConfig.toggleSetupDone(true);
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
                Logger.sendSimpleMessage(player, "&6Setup complete. ResurrectionChest will keep working with your current configuration.");
                Logger.sendSimpleMessage(player, "&7Use &a/resurrectionchest setup &7whenever you want to manage Nightbreak content.");
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
            }
        };
    }

    private static MenuButton createUseCurrentContentItem() {
        return new MenuButton(ItemStackGenerator.generateItemStack(
                Material.RED_STAINED_GLASS_PANE,
                "&cUse Current Content",
                List.of("&cDismisses the setup prompt and keeps your current content state."))) {
            @Override
            public void onClick(Player player) {
                player.closeInventory();
                DefaultConfig.toggleSetupDone(true);
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
                if (RCPackage.getRcPackages().values().stream().anyMatch(RCPackage::isDownloaded)) {
                    Logger.sendSimpleMessage(player, "&aSetup complete. ResurrectionChest will keep using your current installed content.");
                    Logger.sendSimpleMessage(player, "&7Use &a/resurrectionchest setup &7if you want to change or update it later.");
                } else {
                    Logger.sendSimpleMessage(player, "&aSetup complete. ResurrectionChest will continue using vanilla death chests.");
                    Logger.sendSimpleMessage(player, "&7Use &a/resurrectionchest setup &7when you're ready to install optional Nightbreak content.");
                }
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
            }
        };
    }

    private static void sendLink(Player player, String prefix, String display, String hover, String url) {
        player.spigot().sendMessage(
                SpigotMessage.simpleMessage(prefix),
                SpigotMessage.hoverLinkMessage(display, hover, url));
    }

    private static void sendCommand(Player player, String prefix, String display, String hover, String command) {
        player.spigot().sendMessage(
                SpigotMessage.simpleMessage(prefix),
                SpigotMessage.commandHoverMessage(display, hover, command));
    }
}
