# ResurrectionChest

ResurrectionChest is a Spigot/Paper plugin that stores a player's death drops inside a registered death chest instead
of leaving the items scattered at the death location. It works with vanilla chests by default and can optionally display
custom FreeMinecraftModels chest props for Nightbreak content packs.

## Features

- **Death chest storage** -- Moves player death drops to the player's registered resurrection chest.
- **Sign-based setup** -- Players create a death chest by placing a chest and attaching a sign with the configured
  trigger text, `[DeathChest]` by default.
- **XP preservation** -- Can store a configurable percentage of the player's XP in the death chest flow.
- **Armor durability penalty** -- Optionally lowers worn armor durability when a player dies.
- **Missing-chest handling** -- Detects missing or removed registered chests and notifies the player.
- **Particle effects and name tags** -- Highlights registered chests with configurable particles and display text.
- **Optional custom models** -- Integrates with FreeMinecraftModels for placed Wooden Chest and Angelic Chest props.
- **Nightbreak content management** -- Setup, download-all, and update flows for ResurrectionChest content packs.

## Requirements

- Java 21.
- A Spigot or Paper server. `plugin.yml` declares `api-version: 1.21.4`; the build compiles against the Spigot
  `1.21.4` API.
- No hard plugin dependencies. FreeMinecraftModels is a soft dependency used only for optional custom chest visuals.
  MagmaCore is shaded into the plugin jar.

## Installation

1. Drop `ResurrectionChest.jar` into your server's `plugins/` folder.
2. Start the server once to generate the configuration files.
3. Give players `resurrectionchest.use` if your permissions plugin does not use Bukkit defaults.
4. To create a vanilla resurrection chest, place a chest and attach a sign containing `[DeathChest]`.
5. Optional: install FreeMinecraftModels, then run `/resurrectionchest setup` or `/resurrectionchest downloadall` to
   install Nightbreak-managed chest model packs.

## Commands

All commands are under `/resurrectionchest`.

| Command | Description |
| --- | --- |
| `/resurrectionchest` | Shows plugin info and points admins to setup commands. |
| `/resurrectionchest setup` | Opens the Nightbreak content setup and management menu. |
| `/resurrectionchest initialize` | Opens the first-time setup flow. |
| `/resurrectionchest downloadall` | Downloads all available ResurrectionChest content packs for linked Nightbreak accounts. |
| `/resurrectionchest updatecontent` | Updates installed ResurrectionChest content packs. |
| `/resurrectionchest reload` | Reloads the plugin. |

## Permissions

| Permission | Default | Grants |
| --- | --- | --- |
| `resurrectionchest.*` | op | Admin access to setup, initialize, Nightbreak content, and reload commands. |
| `resurrectionchest.setup` | op | Access to `/resurrectionchest setup`. |
| `resurrectionchest.initialize` | op | Access to `/resurrectionchest initialize`. |
| `resurrectionchest.use` | true | Lets players create and use resurrection chests. |
| `resurrectionchest.model.free` | true | Lets players use the free death chest model pack. |
| `resurrectionchest.model.premium` | op | Lets players use the premium death chest model pack. |

## Configuration

On first run, ResurrectionChest generates `plugins/ResurrectionChest/config.yml` and player data files. Important
settings include:

- `Input name for death chest` -- Sign text used to register a chest, defaulting to `[DeathChest]`.
- `Enable high compatibility / low security mode for plugin conflicts` -- Compatibility mode for servers with plugins
  that interfere with chest/sign checks.
- `Lower worn armor's durability on death` and `Amount of durability to lower on death` -- Armor durability penalty.
- `storeXP` and `xpPercentageKept` -- Whether XP is moved through the death chest flow and how much is kept.
- `blacklistedWorlds` -- Worlds where death chest behavior is disabled.
- `deathChestNameTag` and particle settings -- Visual feedback around registered chests.
- Model names for the free and premium single/double chest props.

## Content Packs

ResurrectionChest ships with Nightbreak content-pack definitions:

- **Wooden Chest** -- Free custom chest model pack.
- **Angelic Chest** -- Premium custom chest model pack.

When FreeMinecraftModels is installed, the setup menu can install or uninstall these packs and reload FMM so the chest
props refresh. Without FreeMinecraftModels, ResurrectionChest continues to work as a vanilla chest-based plugin.

## Building From Source

The project builds with Gradle (wrapper included) and shades runtime dependencies into a single jar:

```bash
./gradlew shadowJar
```

On Windows:

```bat
gradlew.bat shadowJar
```

The shaded jar is written to `build/libs/ResurrectionChest.jar`. When `MC_DIST_DIR` or `mcDistDir` is set, the build
also mirrors the jar into that shared dist folder.

## Repository

Maven:

```xml
<repository>
    <id>magmaguy-repo-releases</id>
    <url>https://repo.magmaguy.com/releases</url>
</repository>

<dependency>
    <groupId>com.magmaguy</groupId>
    <artifactId>ResurrectionChest</artifactId>
    <version>2.1.1</version>
    <scope>provided</scope>
</dependency>
```

Gradle:

```kotlin
repositories {
    maven {
        url = uri("https://repo.magmaguy.com/releases")
    }
}

dependencies {
    compileOnly("com.magmaguy:ResurrectionChest:2.1.1")
}
```

## Links

- [ResurrectionChest on Nightbreak](https://nightbreak.io/plugin/resurrectionchest/)
- [ResurrectionChest on Spigot](https://www.spigotmc.org/resources/57541/)
- [Discord](https://discord.gg/nightbreak)

## License

ResurrectionChest is licensed under the GNU General Public License v3.0.
