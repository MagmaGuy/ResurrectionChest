package com.magmaguy.resurrectionchest.events;

import com.magmaguy.magmacore.MagmaCore;
import com.magmaguy.resurrectionchest.MetadataHandler;
import com.magmaguy.resurrectionchest.PersistentObjectHandler;
import com.magmaguy.resurrectionchest.ResurrectionChest;
import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import com.magmaguy.resurrectionchest.configs.PlayerDataConfig;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeathChestConstructorAndRemoverTest {
    private ServerMock server;
    private JavaPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin("ResurrectionChest");
        MagmaCore.createInstance(plugin);
        MetadataHandler.PLUGIN = plugin;
        ResurrectionChest.plugin = plugin;
        new PlayerDataConfig();
        DefaultConfig.blacklistedWorlds = List.of();
        DefaultConfig.resurrectionChestSignName = "[ResChest]";
        DefaultConfig.chestCreationMessage = "&aCreated.";
        DefaultConfig.chestDestructionMessage = "&cDestroyed.";
        DefaultConfig.deathChestRemovedMessage = "&cRemoved.";
        ResurrectionChestObject.getResurrectionChests().clear();
        PersistentObjectHandler.shutdown();
    }

    @AfterEach
    void tearDown() throws Exception {
        ResurrectionChestObject.getResurrectionChests().clear();
        PersistentObjectHandler.shutdown();
        MetadataHandler.PLUGIN = null;
        ResurrectionChest.plugin = null;
        MagmaCore.shutdown(plugin);
        setStaticField(MagmaCore.class, "instance", null);
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    @Test
    void signConstructorRegistersChestFormatsSignAndPersistsPlayerData() {
        World world = server.addSimpleWorld("constructor_world");
        world.loadChunk(0, 0);
        PlayerMock player = playerWithUsePermission();
        Location chestLocation = placeChestWithNorthSign(world);
        Block signBlock = chestLocation.getBlock().getRelative(BlockFace.NORTH);
        SignChangeEvent event = new SignChangeEvent(signBlock, player, new String[]{"[ResChest]", "", "", ""});

        new DeathChestConstructor().onSignPlace(event);

        ResurrectionChestObject object = ResurrectionChestObject.getResurrectionChest(player);
        assertNotNull(object);
        assertSame(object, ResurrectionChestObject.getResurrectionChest(chestLocation));
        assertSame(object, ResurrectionChestObject.getResurrectionChest(signBlock.getLocation()));
        assertEquals("", event.getLine(0));
        assertEquals(ChatColor.translateAlternateColorCodes('&', "&5[ResChest]"), event.getLine(1));
        assertEquals(player.getDisplayName(), event.getLine(2));
        assertEquals(ChatColor.translateAlternateColorCodes('&', "&aCreated."), player.nextMessage());

        assertTrue(PlayerDataConfig.getInstance().getFileConfiguration()
                .getString(player.getUniqueId() + ".location", "")
                .contains("world=constructor_world"));
        assertEquals("none", PlayerDataConfig.getInstance().getFileConfiguration()
                .getString(player.getUniqueId() + ".chestModel"));
    }

    @Test
    void breakingTrackedDeathChestRemovesObjectSignAndPlayerData() {
        World world = server.addSimpleWorld("remover_world");
        world.loadChunk(0, 0);
        PlayerMock player = playerWithUsePermission();
        Location chestLocation = placeChestWithNorthSign(world);
        Block signBlock = chestLocation.getBlock().getRelative(BlockFace.NORTH);
        SignChangeEvent createEvent = new SignChangeEvent(signBlock, player, new String[]{"[ResChest]", "", "", ""});
        new DeathChestConstructor().onSignPlace(createEvent);
        PlayerDataConfig.addPlayerdata(player.getUniqueId(), chestLocation, "none");
        assertNotNull(PlayerDataConfig.getInstance().getFileConfiguration().getString(player.getUniqueId() + ".location"));
        assertEquals(ChatColor.translateAlternateColorCodes('&', "&aCreated."), player.nextMessage());

        new DeathChestRemover().onDeathChestBreak(new BlockBreakEvent(chestLocation.getBlock(), player));

        assertFalse(ResurrectionChestObject.getResurrectionChests().containsKey(player.getUniqueId()));
        assertEquals(Material.AIR, signBlock.getType());
        assertEquals(null, PlayerDataConfig.getInstance().getFileConfiguration().getString(player.getUniqueId() + ".location"));
        assertEquals(ChatColor.translateAlternateColorCodes('&', "&cDestroyed."), player.nextMessage());
        assertTrue(player.nextMessage().contains(ChatColor.translateAlternateColorCodes('&', "&cRemoved.")));
    }

    @Test
    void editingTrackedSignIsCancelledWithoutRemovingTheChest() {
        World world = server.addSimpleWorld("sign_edit_world");
        world.loadChunk(0, 0);
        PlayerMock player = playerWithUsePermission();
        Location chestLocation = placeChestWithNorthSign(world);
        Block signBlock = chestLocation.getBlock().getRelative(BlockFace.NORTH);
        SignChangeEvent createEvent = new SignChangeEvent(signBlock, player, new String[]{"[ResChest]", "", "", ""});
        new DeathChestConstructor().onSignPlace(createEvent);
        SignChangeEvent editEvent = new SignChangeEvent(signBlock, player, new String[]{"oops", "", "", ""});

        new DeathChestRemover().onEditSign(editEvent);

        assertTrue(editEvent.isCancelled());
        assertNotNull(ResurrectionChestObject.getResurrectionChest(player));
    }

    private PlayerMock playerWithUsePermission() {
        PlayerMock player = server.addPlayer("Keeper");
        player.addAttachment(plugin, "resurrectionchest.use", true);
        return player;
    }

    private static Location placeChestWithNorthSign(World world) {
        Location chestLocation = new Location(world, 2, 65, 2);
        Block chestBlock = chestLocation.getBlock();
        chestBlock.setType(Material.CHEST);
        Block signBlock = chestBlock.getRelative(BlockFace.NORTH);
        signBlock.setType(Material.OAK_WALL_SIGN);
        WallSign wallSign = (WallSign) Material.OAK_WALL_SIGN.createBlockData();
        wallSign.setFacing(BlockFace.NORTH);
        signBlock.setBlockData(wallSign);
        return chestLocation;
    }

    private static void setStaticField(Class<?> type, String fieldName, Object value) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }
}
