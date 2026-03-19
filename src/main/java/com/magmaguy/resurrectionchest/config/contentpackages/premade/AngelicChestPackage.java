package com.magmaguy.resurrectionchest.config.contentpackages.premade;

import com.magmaguy.resurrectionchest.config.contentpackages.ContentPackageConfigFields;

import java.util.List;

public class AngelicChestPackage extends ContentPackageConfigFields {
    public AngelicChestPackage() {
        super("angelic_chest",
                true,
                "&6Angelic Chest",
                List.of("&fPremium ResurrectionChest model pack for players with the premium model permission."),
                "https://nightbreak.io/plugin/resurrectionchest/",
                "resurrectionchest");
        setNightbreakSlug("angelic-chest");
        setContentFilePrefixes(List.of("resurrectionchest_angelic_"));
        setCategory(Category.PREMIUM);
    }
}
