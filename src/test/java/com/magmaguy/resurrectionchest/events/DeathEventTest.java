package com.magmaguy.resurrectionchest.events;

import com.magmaguy.resurrectionchest.MetadataHandler;
import com.magmaguy.resurrectionchest.PersistentObjectHandler;
import com.magmaguy.resurrectionchest.ResurrectionChest;
import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeathEventTest {

    private ServerMock server;
    private PluginMock plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin("ResurrectionChest");
        MetadataHandler.PLUGIN = plugin;
        ResurrectionChest.plugin = plugin;
        DefaultConfig.blacklistedWorlds = List.of();
        DefaultConfig.storeXP = false;
        DefaultConfig.deathMessage = "Stored your drops.";
        DefaultConfig.chestMissingMessage = "Your death chest is missing.";
        ResurrectionChestObject.getResurrectionChests().clear();
        PersistentObjectHandler.shutdown();
    }

    @AfterEach
    void tearDown() {
        ResurrectionChestObject.getResurrectionChests().clear();
        PersistentObjectHandler.shutdown();
        MetadataHandler.PLUGIN = null;
        ResurrectionChest.plugin = null;
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    @Test
    void deathMovesDropsIntoRegisteredChest() {
        World world = server.addSimpleWorld("death_world");
        world.loadChunk(0, 0);
        PlayerMock player = playerInWorld(world);
        Location chestLocation = placeChest(world, 2, 65, 2);
        new ResurrectionChestObject(player.getUniqueId(), chestLocation, "none");
        PlayerDeathEvent event = deathEvent(player,
                new ItemStack(Material.DIAMOND, 3),
                new ItemStack(Material.OAK_LOG, 5));

        new DeathEvent().onDeath(event);

        Inventory inventory = ((Chest) chestLocation.getBlock().getState()).getInventory();
        assertTrue(event.getDrops().isEmpty());
        assertEquals(3, amountOf(inventory, Material.DIAMOND));
        assertEquals(5, amountOf(inventory, Material.OAK_LOG));
        assertEquals("Stored your drops.", player.nextMessage());
    }

    @Test
    void deathLeavesDropsWhenRegisteredChestIsFull() {
        World world = server.addSimpleWorld("full_chest_world");
        world.loadChunk(0, 0);
        PlayerMock player = playerInWorld(world);
        Location chestLocation = placeChest(world, 4, 65, 4);
        new ResurrectionChestObject(player.getUniqueId(), chestLocation, "none");
        Inventory inventory = ((Chest) chestLocation.getBlock().getState()).getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, new ItemStack(Material.DIRT, 64));
        }

        PlayerDeathEvent event = deathEvent(player,
                new ItemStack(Material.DIAMOND, 3),
                new ItemStack(Material.OAK_LOG, 5));

        new DeathEvent().onDeath(event);

        assertEquals(2, event.getDrops().size());
        assertEquals(3, amountOf(event.getDrops(), Material.DIAMOND));
        assertEquals(5, amountOf(event.getDrops(), Material.OAK_LOG));
        assertEquals(0, amountOf(inventory, Material.DIAMOND));
        assertEquals(0, amountOf(inventory, Material.OAK_LOG));
        assertNotNull(player.nextMessage(), "The player should be warned that some drops overflowed.");
        assertEquals("Stored your drops.", player.nextMessage());
    }

    private PlayerMock playerInWorld(World world) {
        PlayerMock player = server.addPlayer("Fallen");
        player.teleport(new Location(world, 0, 65, 0));
        player.addAttachment(plugin, "resurrectionchest.use", true);
        return player;
    }

    private static Location placeChest(World world, int x, int y, int z) {
        Location location = new Location(world, x, y, z);
        location.getBlock().setType(Material.CHEST);
        return location;
    }

    private static PlayerDeathEvent deathEvent(PlayerMock player, ItemStack... drops) {
        return new PlayerDeathEvent(player, null, new ArrayList<>(List.of(drops)), 0, 0, 0, 0, "death");
    }

    private static int amountOf(Inventory inventory, Material material) {
        int amount = 0;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null && itemStack.getType() == material) {
                amount += itemStack.getAmount();
            }
        }
        return amount;
    }

    private static int amountOf(List<ItemStack> itemStacks, Material material) {
        int amount = 0;
        for (ItemStack itemStack : itemStacks) {
            if (itemStack != null && itemStack.getType() == material) {
                amount += itemStack.getAmount();
            }
        }
        return amount;
    }
}
