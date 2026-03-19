package com.magmaguy.resurrectionchest.config.contentpackages.premade;

import com.magmaguy.resurrectionchest.config.contentpackages.ContentPackageConfigFields;

import java.util.List;

public class WoodenChestPackage extends ContentPackageConfigFields {
    public WoodenChestPackage() {
        super("wooden_chest",
                true,
                "&2Wooden Chest",
                List.of("&fThe classic ResurrectionChest model pack for players with the free model permission."),
                "https://nightbreak.io/plugin/resurrectionchest/",
                "resurrectionchest");
        setNightbreakSlug("wooden-chest");
        setContentFilePrefixes(List.of("resurrectionchest_free_"));
        setCategory(Category.FREE);
    }
}
