package com.magmaguy.resurrectionchest.events;

import com.magmaguy.resurrectionchest.MetadataHandler;
import com.magmaguy.resurrectionchest.PersistentObjectHandler;
import com.magmaguy.resurrectionchest.ResurrectionChest;
import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import com.magmaguy.resurrectionchest.configs.PlayerDataConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class DeathStorageTest {
    @TempDir Path directory;
    private ServerMock server;
    private PlayerMock player;
    private Chest chest;

    @BeforeEach
    void registerProductionHandler() {
        server = MockBukkit.mock();
        Plugin plugin = MockBukkit.createMockPlugin("ResurrectionChest");
        ResurrectionChest.plugin = MetadataHandler.PLUGIN = plugin;
        new PlayerDataConfig(directory.resolve("playerData.yml").toFile());
        DefaultConfig.blacklistedWorlds = List.of();
        DefaultConfig.enableHighCompatibility = false;
        DefaultConfig.enableDurabilityLossOnDeath = false;
        DefaultConfig.storeXP = true;
        DefaultConfig.xpPercentage = 0.75;
        DefaultConfig.deathMessage = "Stored.";
        DefaultConfig.chestMissingMessage = "Missing.";
        DefaultConfig.chestDestructionMessage = "Removed.";
        DefaultConfig.deathChestRemovedMessage = "Removed.";
        player = server.addPlayer();
        player.addAttachment(plugin, "resurrectionchest.use", true);
        Location location = new Location(server.addSimpleWorld("world"), 8, 64, 8);
        location.getChunk().load();
        location.getBlock().setType(Material.CHEST);
        player.teleport(location.clone().add(0, 1, 0));
        ResurrectionChestObject registration = new ResurrectionChestObject(player, location);
        assertTrue(registration.markTrackedBlocks());
        chest = (Chest) location.getBlock().getState();
        server.getPluginManager().registerEvents(new DeathEvent(), plugin);
    }

    @AfterEach
    void closeServer() {
        ResurrectionChestObject.shutdown();
        ResurrectionChestObject.getResurrectionChests().clear();
        PersistentObjectHandler.shutdown();
        MockBukkit.unmock();
        ResurrectionChest.plugin = MetadataHandler.PLUGIN = null;
    }

    @Test
    void deathStoresItemsAndOnlyTheOwnedChestReturnsExperienceOnce() {
        player.giveExp(100);
        ItemStack diamonds = new ItemStack(Material.DIAMOND, 3);
        ItemStack shield = new ItemStack(Material.SHIELD);
        ItemStack armor = new ItemStack(Material.IRON_CHESTPLATE);
        player.getInventory().setChestplate(armor.clone());
        DefaultConfig.enableDurabilityLossOnDeath = true;
        DefaultConfig.durabilityToLower = 100;
        PlayerDeathEvent death = death(diamonds, shield, armor);
        server.getPluginManager().callEvent(death);
        assertTrue(death.getDrops().isEmpty());
        assertEquals(0, death.getDroppedExp());
        assertEquals(diamonds, chest.getInventory().getItem(0));
        assertEquals(shield, chest.getInventory().getItem(1));
        assertEquals(Material.IRON_CHESTPLATE, chest.getInventory().getItem(2).getType());
        assertEquals(100, ((Damageable) chest.getInventory().getItem(2).getItemMeta()).getDamage());
        assertEquals(5, Arrays.stream(chest.getInventory().getContents()).filter(Objects::nonNull).mapToInt(ItemStack::getAmount).sum());
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0);
        player.openInventory(server.createInventory(null, 27));
        assertEquals(0, player.getTotalExperience());
        player.openInventory(chest.getInventory());
        assertEquals(75, player.getTotalExperience());
        player.closeInventory();
        player.openInventory(chest.getInventory());
        assertEquals(75, player.getTotalExperience());
    }

    @Test
    void overflowLeavesOnlyTheUnstoredRemainderInDeathDrops() {
        for (int slot = 0; slot < 26; slot++) chest.getInventory().setItem(slot, new ItemStack(Material.COBBLESTONE, 64));
        chest.getInventory().setItem(26, new ItemStack(Material.DIAMOND, 63));
        ItemStack shield = new ItemStack(Material.SHIELD);
        PlayerDeathEvent death = death(new ItemStack(Material.DIAMOND, 3), shield);
        server.getPluginManager().callEvent(death);
        assertEquals(List.of(new ItemStack(Material.DIAMOND, 2), shield), death.getDrops());
        assertEquals(new ItemStack(Material.DIAMOND, 64), chest.getInventory().getItem(26));
        for (int slot = 0; slot < 26; slot++) assertEquals(new ItemStack(Material.COBBLESTONE, 64), chest.getInventory().getItem(slot));
    }

    @ParameterizedTest(name = "{displayName} [{index}] {arguments}")
    @ValueSource(strings = {"permission", "blacklisted-world", "keep-inventory", "replaced-chest"})
    void ineligibleDeathsPreserveDropsAndExperience(String reason) {
        ItemStack diamonds = new ItemStack(Material.DIAMOND, 2);
        player.giveExp(100);
        PlayerDeathEvent death = death(diamonds);
        switch (reason) {
            case "permission" -> player.addAttachment(ResurrectionChest.plugin, "resurrectionchest.use", false);
            case "blacklisted-world" -> DefaultConfig.blacklistedWorlds = List.of(player.getWorld().getName());
            case "keep-inventory" -> death.setKeepInventory(true);
            case "replaced-chest" -> {
                chest.getBlock().setType(Material.STONE);
                chest.getBlock().setType(Material.CHEST);
            }
            default -> throw new IllegalArgumentException(reason);
        }
        server.getPluginManager().callEvent(death);
        assertEquals(List.of(diamonds), death.getDrops());
        assertEquals(50, death.getDroppedExp());
        assertTrue(((Chest) chest.getBlock().getState()).getInventory().isEmpty());
        if (reason.equals("replaced-chest")) assertNull(ResurrectionChestObject.getResurrectionChest(player));
    }

    private PlayerDeathEvent death(ItemStack... drops) {
        return new PlayerDeathEvent(player, DamageSource.builder(DamageType.GENERIC).build(),
                new ArrayList<>(List.of(drops)), 50, Component.text("died"), true);
    }
}
