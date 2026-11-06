package com.magmaguy.resurrectionchest.config.contentpackages;

import com.magmaguy.magmacore.config.CustomConfigFields;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ContentPackageConfigFields extends CustomConfigFields {
    @Getter
    @Setter
    private int version = 0;
    @Getter
    private String name;
    @Getter
    private List<String> description;
    @Getter
    private String downloadLink;
    private String folderName;
    @Getter
    @Setter
    private String nightbreakSlug;
    @Getter
    @Setter
    private List<String> contentFilePrefixes;
    @Getter
    @Setter
    private Category category = Category.FREE;

    public ContentPackageConfigFields(String filename,
                                      boolean isEnabled,
                                      String name,
                                      List<String> description,
                                      String downloadLink,
                                      String folderName) {
        super(filename, isEnabled);
        this.name = name;
        this.description = description;
        this.downloadLink = downloadLink;
        this.folderName = folderName;
        this.contentFilePrefixes = List.of(folderName);
    }

    public ContentPackageConfigFields(String filename, boolean isEnabled) {
        super(filename, isEnabled);
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        this.name = processString("name", name, null, true);
        this.description = processStringList("description", description, null, true);
        this.downloadLink = processString("downloadLink", downloadLink, downloadLink, false);
        // ResurrectionChest's package archives carry only models, so a download never rewrites the saved version.
        // A premade package's compiled version therefore wins over a lower one saved earlier; otherwise the package
        // stays outdated, and every automatic update downloads it again for the next start to import.
        this.version = Math.max(version, processInt("version", version, 0, true));
        this.folderName = processString("folderName", folderName, null, true);
        this.nightbreakSlug = processString("nightbreakSlug", nightbreakSlug, null, false);
        this.contentFilePrefixes = processStringList("contentFilePrefixes", contentFilePrefixes, List.of(folderName), false);
        this.category = processEnum("category", category, Category.FREE, Category.class, false);
    }

    public enum Category {
        FREE,
        PREMIUM
    }
}
