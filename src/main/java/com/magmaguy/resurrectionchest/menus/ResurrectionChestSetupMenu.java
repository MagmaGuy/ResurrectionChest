package com.magmaguy.resurrectionchest.menus;

import com.magmaguy.magmacore.menus.MenuButton;
import com.magmaguy.magmacore.menus.SetupMenu;
import com.magmaguy.magmacore.nightbreak.DownloadAllContentPackage;
import com.magmaguy.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import com.magmaguy.resurrectionchest.MetadataHandler;
import com.magmaguy.resurrectionchest.config.contentpackages.ContentPackageConfigFields;
import com.magmaguy.resurrectionchest.content.RCPackage;
import com.magmaguy.resurrectionchest.content.RCPackageRefresher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ResurrectionChestSetupMenu {
    private ResurrectionChestSetupMenu() {
    }

    public static void createMenu(Player player) {
        List<RCPackage> packages = new ArrayList<>(RCPackage.getRcPackages().values()).stream()
                .sorted(Comparator.comparing(pkg ->
                        ChatColor.stripColor(ChatColorConverter.convert(pkg.getContentPackageConfigFields().getName()))))
                .collect(Collectors.toList());
        RCPackageRefresher.refreshContentAndAccess();

        MenuButton infoButton = new MenuButton(ItemStackGenerator.generateSkullItemStack("magmaguy",
                "&2Installation instructions:",
                List.of(
                        "&61) &fLink your Nightbreak account: &a/nightbreaklogin",
                        "&62) &fDownload chest packs: &a/resurrectionchest downloadall",
                        "&63) &fOr browse and manage them here: &a/resurrectionchest setup",
                        "&7Custom chest props require FreeMinecraftModels.",
                        "&7ResurrectionChest still works with vanilla chests without it."))) {
            @Override
            public void onClick(Player p) {
                p.closeInventory();
                Logger.sendSimpleMessage(p, "<g:#8B0000:#CC4400:#DAA520>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</g>");
                Logger.sendSimpleMessage(p, "&6&lResurrectionChest installation resources:");
                p.spigot().sendMessage(
                        SpigotMessage.simpleMessage("&2&lNightbreak account: "),
                        SpigotMessage.hoverLinkMessage("&ahttps://nightbreak.io/account/",
                                "&7Click to open the Nightbreak account page.",
                                "https://nightbreak.io/account/"));
                p.spigot().sendMessage(
                        SpigotMessage.simpleMessage("&2&lContent: "),
                        SpigotMessage.hoverLinkMessage("&ahttps://nightbreak.io/plugin/resurrectionchest/",
                                "&7Click to browse ResurrectionChest content.",
                                "https://nightbreak.io/plugin/resurrectionchest/"));
                p.spigot().sendMessage(
                        SpigotMessage.simpleMessage("&2&lFreeMinecraftModels: "),
                        SpigotMessage.hoverLinkMessage("&ahttps://nightbreak.io/plugin/freeminecraftmodels/",
                                "&7Click to open the FreeMinecraftModels page.",
                                "https://nightbreak.io/plugin/freeminecraftmodels/"));
                p.spigot().sendMessage(
                        SpigotMessage.commandHoverMessage("&2&lBulk download: &a/resurrectionchest downloadall",
                                "&7Click to download all available ResurrectionChest content.",
                                "/resurrectionchest downloadall"));
                if (NightbreakAccount.hasToken()) {
                    p.spigot().sendMessage(
                            SpigotMessage.commandHoverMessage("&2&lBulk update: &a/resurrectionchest updatecontent",
                                    "&7Click to update all outdated ResurrectionChest content.",
                                    "/resurrectionchest updatecontent"));
                }
                if (Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
                    p.spigot().sendMessage(
                            SpigotMessage.commandHoverMessage("&2&lFMM setup: &a/fmm initialize",
                                    "&7Click to open the FreeMinecraftModels first-time setup flow.",
                                    "/fmm initialize"));
                } else {
                    Logger.sendSimpleMessage(p, "&eInstall FreeMinecraftModels if you want ResurrectionChest to display custom chest props.");
                }
                Logger.sendSimpleMessage(p, "<g:#8B0000:#CC4400:#DAA520>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</g>");
            }
        };

        List<com.magmaguy.magmacore.menus.ContentPackage> allPackages = new ArrayList<>(packages);
        allPackages.add(new DownloadAllContentPackage<>(() -> new ArrayList<>(RCPackage.getRcPackages().values()),
                "ResurrectionChest",
                "https://nightbreak.io/plugin/resurrectionchest/",
                "resurrectionchest downloadall"));

        new SetupMenu((JavaPlugin) MetadataHandler.PLUGIN, player, infoButton, allPackages,
                List.of(
                        createFilter(packages, Material.CHEST, "Free Packs", rcPackage ->
                                rcPackage.getContentPackageConfigFields().getCategory() == ContentPackageConfigFields.Category.FREE),
                        createFilter(packages, Material.TOTEM_OF_UNDYING, "Premium Packs", rcPackage ->
                                rcPackage.getContentPackageConfigFields().getCategory() == ContentPackageConfigFields.Category.PREMIUM)),
                "Setup menu");
    }

    private static SetupMenu.SetupMenuFilter createFilter(List<RCPackage> orderedPackages,
                                                          Material material,
                                                          String name,
                                                          Predicate<RCPackage> predicate) {
        return new SetupMenu.SetupMenuFilter(
                ItemStackGenerator.generateItemStack(material, name),
                orderedPackages.stream().filter(predicate).toList());
    }
}
