package com.magmaguy.resurrectionchest.menus;

import com.magmaguy.magmacore.menus.MenuButton;
import com.magmaguy.magmacore.menus.SetupMenuBuilder;
import com.magmaguy.magmacore.nightbreak.DownloadAllContentPackage;
import com.magmaguy.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.magmacore.nightbreak.NightbreakSetupControls;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import com.magmaguy.resurrectionchest.MetadataHandler;
import com.magmaguy.resurrectionchest.ResurrectionChest;
import com.magmaguy.resurrectionchest.config.contentpackages.ContentPackageConfigFields;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
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
import java.util.stream.Collectors;

public class ResurrectionChestSetupMenu {
    private ResurrectionChestSetupMenu() {
    }

    public static void createMenu(Player player) {
        if (!DefaultConfig.isSetupDone()) {
            DefaultConfig.toggleSetupDone(true);
        }

        List<RCPackage> packages = new ArrayList<>(RCPackage.getRcPackages().values()).stream()
                .sorted(Comparator.comparing(pkg ->
                        ChatColor.stripColor(ChatColorConverter.convert(pkg.getContentPackageConfigFields().getName()))))
                .collect(Collectors.toList());
        RCPackageRefresher.refreshContentAndAccess();

        MenuButton infoButton = NightbreakSetupControls.setupInfoButton(
                ResurrectionChest.NIGHTBREAK_PLUGIN_SPEC,
                "https://nightbreak.io/plugin/resurrectionchest/#setup");

        SetupMenuBuilder builder = new SetupMenuBuilder((JavaPlugin) MetadataHandler.PLUGIN, player)
                .title("Setup menu")
                .infoButton(infoButton)
                .packages(packages)
                .appendPackage(new DownloadAllContentPackage<>(() -> new ArrayList<>(RCPackage.getRcPackages().values()),
                        "ResurrectionChest",
                        "https://nightbreak.io/plugin/resurrectionchest/",
                        "resurrectionchest downloadall"))
                .addFilter(Material.CHEST, "Free Packs",
                        (java.util.function.Predicate<RCPackage>) rcPackage ->
                                rcPackage.getContentPackageConfigFields().getCategory() == ContentPackageConfigFields.Category.FREE)
                .addFilter(Material.TOTEM_OF_UNDYING, "Premium Packs",
                        (java.util.function.Predicate<RCPackage>) rcPackage ->
                                rcPackage.getContentPackageConfigFields().getCategory() == ContentPackageConfigFields.Category.PREMIUM);
        NightbreakSetupControls.prependStandardControls(builder, (JavaPlugin) MetadataHandler.PLUGIN, ResurrectionChest.NIGHTBREAK_PLUGIN_SPEC)
                .open();
    }
}
