package com.magmaguy.resurrectionchest.config.contentpackages;

import com.magmaguy.magmacore.config.CustomConfig;
import com.magmaguy.resurrectionchest.content.RCPackage;
import lombok.Getter;

import java.util.HashMap;

public class ContentPackageConfig extends CustomConfig {
    @Getter
    private static final HashMap<String, ContentPackageConfigFields> contentPackages = new HashMap<>();

    public ContentPackageConfig() {
        super("content_packages", "com.magmaguy.resurrectionchest.config.contentpackages.premade", ContentPackageConfigFields.class);
        contentPackages.clear();
        for (String key : super.getCustomConfigFieldsHashMap().keySet()) {
            ContentPackageConfigFields fields = (ContentPackageConfigFields) super.getCustomConfigFieldsHashMap().get(key);
            contentPackages.put(key, fields);
            new RCPackage(fields);
        }
    }
}
