package com.magmaguy.resurrectionchest.config.contentpackages;

import com.magmaguy.resurrectionchest.config.contentpackages.premade.AngelicChestPackage;
import com.magmaguy.resurrectionchest.config.contentpackages.premade.WoodenChestPackage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A package's local version decides whether Nightbreak content counts as outdated. ResurrectionChest's archives
 * carry only models, so a download never rewrites the saved version: a premade package's compiled version has to
 * win over the 0 that earlier releases saved, or every automatic update downloads the package again.
 */
class ContentPackageVersionTest {

    private static ContentPackageConfigFields load(ContentPackageConfigFields fields, Integer savedVersion) {
        YamlConfiguration saved = new YamlConfiguration();
        if (savedVersion != null) saved.set("version", savedVersion);
        fields.setFileConfiguration(saved);
        fields.processConfigFields();
        return fields;
    }

    @Test
    void packagesMatchThePublishedVersion() {
        assertEquals(1, load(new WoodenChestPackage(), null).getVersion());
        assertEquals(1, load(new AngelicChestPackage(), null).getVersion());
    }

    @Test
    void compiledVersionWinsOverTheZeroEarlierReleasesSaved() {
        assertEquals(1, load(new WoodenChestPackage(), 0).getVersion());
        assertEquals(1, load(new AngelicChestPackage(), 0).getVersion());
    }

    @Test
    void higherSavedVersionWins() {
        assertEquals(2, load(new WoodenChestPackage(), 2).getVersion());
    }
}
