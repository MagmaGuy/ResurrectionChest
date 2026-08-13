package com.magmaguy.resurrectionchest.config.contentpackages;

import com.magmaguy.magmacore.config.CustomConfig;
import com.magmaguy.resurrectionchest.content.RCPackage;

public class ContentPackageConfig extends CustomConfig {

    public ContentPackageConfig() {
        super("content_packages", "com.magmaguy.resurrectionchest.config.contentpackages.premade", ContentPackageConfigFields.class);
        for (String key : super.getCustomConfigFieldsHashMap().keySet()) {
            ContentPackageConfigFields fields = (ContentPackageConfigFields) super.getCustomConfigFieldsHashMap().get(key);
            new RCPackage(fields);
        }
    }
}
